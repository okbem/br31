
package okbem.br31.matcher.matcher.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


/**
 * 사용자에게 알맞은 상대나 그룹을 찾아 주는 클래스.
 * 선호 규칙을 등록한 사용자들을 관리하고, 그 중에서 서로 취향이 맞는 사용자들을
 * 주기적으로 찾아내어 매치시킨다.
 *
 * @param <U> 사용자 타입
 * @param <R> 규칙 타입
 */
public abstract class Matcher<U, R extends Enum<R>> {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());


    /**
     * 매치 규칙을 담고 있는 클래스.
     * 매치 규칙은 어떠한 기준으로 매치가 이루어졌는지를 나타낸다.
     *
     * @param <R> 규칙 타입
     */
    @lombok.Data
    @lombok.RequiredArgsConstructor
    protected static final class MatchRule<R extends Enum<R>> {

        /**
         * 하나의 매치에 포함되는 사용자의 수.
         */
        private int matchSize;

        /**
         * 서로 매치된 사용자들에게 공통으로 적용되는 규칙.
         */
        // <ruleKey: R, ruleValue: Object>
        private final EnumMap<R, Object> ruleMap;

        /**
         * Copy constructor of {@code MatchRule}.
         *
         * @param matchRule {@code MatchRule} instance to clone
         */
        public MatchRule(MatchRule<R> matchRule) {
            this.matchSize = matchRule.matchSize;
            this.ruleMap = new EnumMap<>(matchRule.ruleMap);
        }

    }


    /**
     * 선호 규칙을 담고 있는 클래스.
     * 어떤 사용자가 매치되기 위해서는 선호 규칙을 등록해야 한다. 선호 규칙은 각
     * 항목별로 원하는 옵션을 여러 개 지정하는 것이 가능하다. 선호 규칙의 모든
     * 항목이 적어도 하나 이상 겹치는 사용자들끼리만 서로 매치될 수 있다.
     *
     * @param <R> 규칙 타입
     */
    @lombok.Data
    private static final class MatchRuleOptions<R extends Enum<R>> {

        /**
         * 선호하는 사용자의 수.
         */
        private final Set<Integer> matchSizeSet;

        /**
         * 선호하는 규칙.
         */
        // <ruleKey: R, ruleValueSet: Set<?>>
        private final EnumMap<R, Set<?>> ruleSetMap;

    }


    /**
     * 규칙 타입의 클래스 객체.
     * 비어 있는 {@link EnumMap} 인스턴스를 생성할 때 사용한다.
     */
    private final Class<R> ruleKeyType;


    /**
     * {@link EnumSet} 타입의 규칙 키 목록.
     * 선호 규칙을 등록할 때 누락된 규칙이 있는지 확인하는 용도로 사용한다.
     */
    private final EnumSet<R> ruleKeySet;


    /**
     * {@link ArrayList} 타입의 규칙 키 목록.
     * 서로 취향이 맞는 사용자들을 찾을 때 규칙 키의 조합 순서로 사용한다.
     */
    private final ArrayList<R> ruleKeyList;


    /**
     * 사용자들이 등록한 선호 규칙을 관리하기 위한 자료 구조.
     * 여러 스레드 간에 공유되므로 사용할 때에는 반드시 동기화 처리를 해야 한다.
     */
    // <user: U, options: MatchRuleOptions<R>>
    private final HashMap<U, MatchRuleOptions<R>> userMap;


    /**
     * {@code Matcher} 인스턴스를 생성한다.
     *
     * @param ruleKeyType 규칙 타입의 클래스 객체
     * @param delay 매 주기마다 추가되는 지연 시간
     */
    public Matcher(Class<R> ruleKeyType, Duration delay) {
        this.ruleKeyType = ruleKeyType;
        this.ruleKeySet = EnumSet.allOf(this.ruleKeyType);
        this.ruleKeyList = new ArrayList<>(this.ruleKeySet);
        this.userMap = new HashMap<>();

        Executors.newSingleThreadScheduledExecutor()
            .scheduleWithFixedDelay(
                this::run,
                delay.toNanos(),
                delay.toNanos(),
                TimeUnit.NANOSECONDS
            );

        logger.info("{} started: ruleKeyType={}, ruleKeySet={}, delay={}",
            this.getClass(),
            this.ruleKeyType.getSimpleName(),
            this.ruleKeySet,
            delay
        );
    }


    /**
     * 매치 결과를 처리한다.
     * 이 메서드는 서로 취향이 맞는 사용자들끼리 매치된 이후에 곧바로 호출된다.
     * 매치 결과를 가지고 어떤 작업을 수행하려면 이 메서드를 구현하면 된다.
     *
     * 만약 매치 결과가 여러 개일 경우에는 한 주기에 두 번 이상 호출될 수 있다.
     *
     * @param matchRule 매치 규칙
     * @param match 서로 매치된 사용자들
     */
    protected abstract void playMatch(MatchRule<R> matchRule, Set<U> match);


    /**
     * 서로 취향이 맞는 사용자들 중에서 매치를 찾는다.
     * 동일한 취향을 가진 사용자들을 어떻게 매치시킬지 결정한다. 이 과정에서
     * 매치되지 않는 사용자가 있을 수 있다.
     *
     * 기본적으로는 주어진 사용자들의 순서를 무작위로 섞고 맨 앞쪽부터 차례대로
     * {@link MatchRule#matchSize}만큼씩 뽑아서 매치시키는 방식으로 작동한다.
     * 만약 이 메서드를 다시 구현하고자 한다면, 하나의 매치에 포함되는 사용자의
     * 수는 반드시 {@link MatchRule#matchSize}와 동일해야 한다.
     *
     * 이 메서드에서 리턴하는 사용자들은 {@link #playMatch(MatchRule, Set)}를
     * 호출할 때 두 번째 파라미터로 사용된다.
     *
     * @param matchRule 매치 규칙
     * @param userSet 서로 취향이 맞는 사용자들
     * @return 서로 매치된 사용자들의 그룹 목록
     */
    protected List<Set<U>> findMatches(MatchRule<R> matchRule, Set<U> userSet) {
        // 주어진 사용자들의 순서를 무작위로 섞는다.
        List<U> userList = new ArrayList<>(userSet);
        Collections.shuffle(userList, ThreadLocalRandom.current());

        // 주어진 사용자들로부터 얼마나 많은 매치를 찾을 수 있는지 계산하고,
        // 서로 매치된 사용자들의 그룹을 저장할 리스트를 생성한다.
        int matchSize = matchRule.matchSize;
        int matchListSize = (userList.size() / matchSize);
        List<Set<U>> matchList = new ArrayList<>(matchListSize);

        // 무작위로 섞인 순서에서 맨 앞쪽부터 차례대로 매치시킨다.
        for (int i = 0; i < (matchListSize * matchSize); i += matchSize)
            matchList.add(new HashSet<>(userList.subList(i, i + matchSize)));

        return matchList;
    }


    /**
     * 사용자를 등록한다.
     * 등록된 사용자는 매치 대기 상태가 되어 서로 취향이 맞는 사용자가 나타날
     * 때까지 기다리게 된다. 그러고 나서 알맞은 상대나 그룹이 발견되면 매치가
     * 이루어지고, 매치된 사용자가 이전에 등록했던 내용은 자동으로 삭제된다.
     *
     * @param user 등록할 사용자
     * @param matchSizeSet 선호하는 사용자의 수
     * @return 등록에 성공하면 {@code true},
     *         동일한 사용자가 이미 등록되어 있으면 {@code false}
     *
     * @see #submit(Object, Set, EnumMap)
     */
    public final boolean submit(U user, Set<Integer> matchSizeSet) {
        return this.submit(user, matchSizeSet, null);
    }


    /**
     * 사용자를 등록한다.
     * 등록된 사용자는 매치 대기 상태가 되어 서로 취향이 맞는 사용자가 나타날
     * 때까지 기다리게 된다. 그러고 나서 알맞은 상대나 그룹이 발견되면 매치가
     * 이루어지고, 매치된 사용자가 이전에 등록했던 내용은 자동으로 삭제된다.
     *
     * @param user 등록할 사용자
     * @param matchSizeSet 선호하는 사용자의 수
     * @param ruleSetMap 선호하는 규칙
     * @return 등록에 성공하면 {@code true},
     *         동일한 사용자가 이미 등록되어 있으면 {@code false}
     *
     * @see #submit(Object, Set)
     */
    public final boolean submit(
        U user,
        Set<Integer> matchSizeSet,
        // <ruleKey: R, ruleValueSet: Set<?>>
        EnumMap<R, Set<?>> ruleSetMap
    ) {
        logger.debug("submit(): user={}, matchSizeSet={}, ruleSetMap={}",
            user,
            matchSizeSet,
            ruleSetMap
        );

        if (user == null)
            throw new IllegalArgumentException("user cannot be null");

        if (matchSizeSet == null || matchSizeSet.isEmpty())
            throw new IllegalArgumentException(
                "matchSizeSet cannot be null or empty"
            );

        if (matchSizeSet.stream().anyMatch(matchSize -> matchSize < 2))
            throw new IllegalArgumentException(
                "matchSize cannot be less than two"
            );

        if (ruleSetMap == null)
            ruleSetMap = new EnumMap<>(this.ruleKeyType);

        if (!Objects.equals(ruleSetMap.keySet(), this.ruleKeySet))
            throw new IllegalArgumentException(
                "ruleSetMap must contain the following keys: " + this.ruleKeySet
            );

        if (ruleSetMap.values().stream().anyMatch(ruleValueSet -> {
            return (ruleValueSet == null || ruleValueSet.isEmpty());
        }))
            throw new IllegalArgumentException(
                "ruleValueSet cannot be null or empty"
            );

        synchronized (this) {
            if (this.userMap.containsKey(user))
                return false;

            this.userMap.put(
                user,
                new MatchRuleOptions<>(matchSizeSet, ruleSetMap)
            );
            return true;
        }
    }


    /**
     * 사용자가 이전에 등록했던 내용을 취소한다.
     * 취소된 사용자는 다시 등록되기 전까지 매치 대상에 포함되지 않는다.
     *
     * 사용자가 매치되면 이전에 등록했던 내용은 자동으로 삭제된다.
     *
     * @param user 취소할 사용자
     * @return 취소에 성공하면 {@code true},
     *         사용자가 등록되어 있지 않으면 {@code false}
     */
    public final boolean cancel(U user) {
        logger.debug("cancel(): user={}", user);

        if (user == null)
            throw new IllegalArgumentException("user cannot be null");

        synchronized (this) {
            if (!this.userMap.containsKey(user))
                return false;

            this.userMap.remove(user);
            return true;
        }
    }


    /**
     * 등록된 사용자들 중에서 서로 취향이 맞는 사용자들을 찾아내어 매치시킨다.
     * 이 메서드는 직접적으로 호출되지 않고 별도의 스레드에서
     * {@link java.util.concurrent.ScheduledExecutorService}에 의해 주기적으로
     * 실행된다.
     */
    private synchronized void run() {
        try {
            Set<U> allUserSet = this.userMap.keySet();

            // 등록된 사용자가 하나도 없으면 더 이상 진행하지 않는다.
            if (allUserSet.isEmpty())
                return;

            logger.debug("run(): allUserSet={}", allUserSet);

            // 이것은 이미 찾은 매치 규칙을 탐색에서 제외시키는 용도로 사용된다.
            Set<MatchRule<R>> oldMatchRuleSet = new HashSet<>();

            // 서로 취향이 맞는 사용자들을 더 이상 찾을 수 없을 때까지 반복한다.
            for (;;) {
                MatchRule<R> matchRule
                    = new MatchRule<>(new EnumMap<>(this.ruleKeyType));

                // 서로 취향이 맞는 사용자들을 찾는다.
                Set<U> finalUserSet = this.findMatchRule(
                    -1,
                    allUserSet,
                    oldMatchRuleSet,
                    matchRule
                );

                if (finalUserSet == null)
                    break;

                logger.debug("run(): {}", matchRule);
                logger.debug("run(): finalUserSet={}", finalUserSet);

                // 이번에 찾은 매치 규칙은 다음 번 탐색에서 제외시킨다.
                oldMatchRuleSet.add(matchRule);

                // 서로 취향이 맞는 사용자들 중에서 매치를 찾는다.
                List<Set<U>> matchList = this.findMatches(
                    new MatchRule<>(matchRule),
                    new HashSet<>(finalUserSet)
                );

                // 서로 매치된 사용자들의 그룹별로 매치 결과를 처리한다.
                for (Set<U> match : matchList) {
                    logger.debug("run(): match={}", match);

                    if (!finalUserSet.containsAll(match))
                        throw new IllegalStateException(
                            "match must be a subset of userSet"
                        );
                    if (match.size() != matchRule.matchSize)
                        throw new IllegalStateException(
                            "match must have exactly the same size as matchSize"
                        );

                    // 매치된 사용자들이 이전에 등록했던 선호 규칙을 삭제하여
                    // 매치 대상에서 제외시킨다.
                    finalUserSet.removeAll(match);
                    allUserSet.removeAll(match);

                    this.playMatch(new MatchRule<>(matchRule), match);
                }

                logger.debug("run(): unmatched={}", finalUserSet);
            }
        } catch (Throwable e) {
            String name = this.getClass().toString();
            logger.error("An exception occurred while running " + name, e);

            throw e;
        }
    }


    /**
     * 서로 취향이 맞는 사용자들을 찾는다.
     * 무작위로 매치 규칙을 정하고 그 매치 규칙을 선호하는 사용자들을 찾는다.
     *
     * 모든 경우에 대한 매치 규칙을 무작위 순서로 탐색하되, 그 매치 규칙을
     * 선호하는 사용자들로부터 적어도 하나 이상의 매치를 찾을 수 있는 경우에
     * 대해서만 탐색을 수행한다. 적절한 매치 규칙을 찾으면 탐색을 종료하고 그
     * 매치 규칙을 선호하는 사용자들을 리턴한다. 모든 경우를 탐색했음에도
     * 불구하고 적절한 매치 규칙을 찾지 못했다면 {@code null}을 리턴한다.
     *
     * 모든 경우에 대한 매치 규칙을 탐색하기 위해서 규칙 키를 하나씩 조합하여
     * 매치 규칙을 생성하는 방법을 사용한다. 규칙 키는 {@link #ruleKeyList}에
     * 나열된 순서대로 조합하며, 규칙 키가 모두 조합되어 하나의 매치 규칙이
     * 완성되면 그 매치 규칙이 적절한지 확인하고 탐색 종료 여부를 판단한다.
     *
     * 어떤 규칙 키를 조합할 차례인지는 {@code depth} 값으로 주어진다. 규칙 키의
     * 순서를 구분하기 위해 번호로 표현하며, -1은 {@link MatchRule#matchSize}를
     * 의미하는 특수한 용도로 사용된다. 이 메서드를 맨 처음 호출하는 경우에는
     * 반드시 {@code depth} 값이 -1이 되어야 한다.
     *
     * @param depth 매치 규칙에 추가할 규칙 키 번호
     * @param userSet 탐색의 대상이 되는 사용자들
     * @param oldMatchRuleSet 탐색에서 제외시킬 매치 규칙의 목록
     * @param matchRule 찾은 매치 규칙을 저장할 변수
     * @return 서로 취향이 맞는 사용자들
     */
    private Set<U> findMatchRule(
        int depth,
        Set<U> userSet,
        Set<MatchRule<R>> oldMatchRuleSet,
        MatchRule<R> matchRule
    ) {
        assert (depth >= -1 && depth < this.ruleKeyList.size());

        // 매치 규칙에 추가해야 하는 규칙 키가 무엇인지 파악한다.
        // 만약 {@code depth} 값이 -1인 경우에는 {@code null}이 된다.
        R ruleKey = null;
        if (depth >= 0)
            ruleKey = this.ruleKeyList.get(depth);

        // 탐색의 대상이 되는 사용자들을 규칙 키의 규칙 값별로 분류한다.
        // <ruleValue: Object, newUserSet: Set<U>>
        Map<Object, Set<U>> invertedUserMap = new HashMap<>();
        for (U user : userSet) {
            MatchRuleOptions<R> options = this.userMap.get(user);

            Set<?> ruleValueSet = options.matchSizeSet;
            if (ruleKey != null)
                ruleValueSet = options.ruleSetMap.get(ruleKey);

            for (Object ruleValue : ruleValueSet) {
                Set<U> newUserSet = invertedUserMap.get(ruleValue);

                if (newUserSet == null) {
                    newUserSet = new HashSet<>();
                    invertedUserMap.put(ruleValue, newUserSet);
                }

                newUserSet.add(user);
            }
        }

        // 매치 규칙에 추가할 규칙 값의 순서를 무작위로 섞는다.
        List<Object> ruleValueList = new ArrayList<>(invertedUserMap.keySet());
        Collections.shuffle(ruleValueList, ThreadLocalRandom.current());

        for (Object ruleValue : ruleValueList) {
            Set<U> newUserSet = invertedUserMap.get(ruleValue);

            // 규칙 키와 규칙 값을 매치 규칙에 추가한다.
            if (ruleKey == null)
                matchRule.matchSize = (Integer)ruleValue;
            else
                matchRule.ruleMap.put(ruleKey, ruleValue);

            // 현재의 매치 규칙을 선호하는 사용자들로부터 적어도 하나 이상의
            // 매치를 찾을 수 있는지 확인한다.
            if (newUserSet.size() < matchRule.matchSize)
                continue;

            if (depth + 1 < this.ruleKeyList.size()) {
                // 아직 매치 규칙이 완성되지 않았다면 다음 규칙 키를 조합한다.
                Set<U> finalUserSet = this.findMatchRule(
                    depth + 1,
                    newUserSet,
                    oldMatchRuleSet,
                    matchRule
                );

                // 적절한 매치 규칙을 찾았다면 탐색을 종료한다.
                if (finalUserSet != null)
                    return finalUserSet;
            } else {
                // 매치 규칙이 완성되었다면 매치 규칙이 제외 대상인지 확인한다.
                if (!oldMatchRuleSet.contains(matchRule))
                    return newUserSet;
            }
        }

        return null;
    }

}

