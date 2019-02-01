
package okbem.br31.matcher.matcher.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * 매치 기능을 검증하기 위한 용도로 사용되는 {@link Matcher}.
 * 예상되는 횟수만큼 매치되었는지 확인할 수 있으며, 매치가 올바르게 이루어졌는지
 * 검증하는 유틸리티 메서드를 제공한다.
 *
 * @param <U> 사용자 타입
 * @param <R> 규칙 타입
 */
public class TestMatcher<U, R extends Enum<R>> extends Matcher<U, R> {

    /**
     * 앞으로 남은 매치 횟수를 세는 동기화 객체.
     * 예상되는 횟수만큼 매치되었는지 확인할 때 사용된다.
     */
    private final CountDownLatch latch;


    /**
     * 매치 횟수를 확인하기 전에 대기해야 하는 최대 시간.
     * 매치가 충분히 일어나게 하기 위해서 얼마나 기다려야 하는지를 나타낸다.
     */
    private final Duration defaultTimeout;


    /**
     * 매치 결과를 저장하기 위한 자료 구조.
     */
    // <ruleMap: EnumMap<R, Object>, matchSet: Set<Set<U>>>
    public final Map<EnumMap<R, Object>, Set<Set<U>>> matchResult;


    /**
     * {@code TestMatcher} 인스턴스를 생성한다.
     *
     * @param ruleKeyType 규칙 타입의 클래스 객체
     * @param delay 매 주기마다 추가되는 지연 시간
     * @param latch 예상되는 매치 횟수
     * @param defaultTimeout 매치 횟수를 확인하기 전에 대기해야 하는 최대 시간
     */
    public TestMatcher(
        Class<R> ruleKeyType,
        Duration delay,
        CountDownLatch latch,
        Duration defaultTimeout
    ) {
        super(ruleKeyType, delay);

        this.latch = latch;
        this.defaultTimeout = defaultTimeout;

        this.matchResult = new HashMap<>();
    }


    /**
     * 매치가 이루어지면 그 결과를 저장하고 매치 횟수를 하나 센다.
     */
    @Override
    protected void playMatch(MatchRule<R> matchRule, Set<U> match) {
        EnumMap<R, Object> ruleMap = new EnumMap<>(matchRule.getRuleMap());

        Set<Set<U>> matchSet = this.matchResult.get(ruleMap);
        if (matchSet == null) {
            matchSet = new HashSet<>();
            this.matchResult.put(ruleMap, matchSet);
        }
        matchSet.add(new HashSet<>(match));

        this.latch.countDown();
    }


    /**
     * 예상되는 횟수만큼 매치되었는지 확인한다.
     * 매치 횟수를 확인하기 전에 매치가 충분히 일어나게 하기 위해서 일정 시간을
     * 기다린다. {@link #defaultTimeout} 값에 따라서 대기하는 시간이 달라진다.
     *
     * @return 예상되는 횟수만큼 매치되었으면 {@code true},
     *         그만큼 매치되지 않았으면 {@code false}
     */
    public final boolean isMatched() {
        return this.isMatched(this.defaultTimeout);
    }


    /**
     * 예상되는 횟수만큼 매치되었는지 확인한다.
     * 매치 횟수를 확인하기 전에 매치가 충분히 일어나게 하기 위해서 일정 시간을
     * 기다린다. 얼마나 대기해야 하는지는 {@code timeout} 값으로 지정할 수 있다.
     *
     * @param timeout 매치 횟수를 확인하기 전에 대기해야 하는 최대 시간
     * @return 예상되는 횟수만큼 매치되었으면 {@code true},
     *         그만큼 매치되지 않았으면 {@code false}
     */
    public final boolean isMatched(Duration timeout) {
        try {
            return this.latch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }


    /**
     * 매치가 올바르게 이루어졌는지 검증한다.
     *
     * @param ruleSetMap 검증의 대상이 되는 매치 규칙의 superset
     * @param matchSize 하나의 매치에 포함되어야 하는 사용자의 수
     * @param matchSetSize 예상되는 매치의 개수
     * @param expectedUsers 매치될 것으로 예상되는 사용자 목록
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final void assertMatch(
        EnumMap<R, Set<?>> ruleSetMap,
        int matchSize,
        int matchSetSize,
        U... expectedUsers
    ) {
        this.assertMatch(
            ruleSetMap,
            matchSize,
            matchSetSize,
            Arrays.asList(expectedUsers)
        );
    }


    /**
     * 매치가 올바르게 이루어졌는지 검증한다.
     *
     * @param ruleSetMap 검증의 대상이 되는 매치 규칙의 superset
     * @param matchSize 하나의 매치에 포함되어야 하는 사용자의 수
     * @param matchSetSize 예상되는 매치의 개수
     * @param expectedUsers 매치될 것으로 예상되는 사용자 목록
     */
    public final void assertMatch(
        EnumMap<R, Set<?>> ruleSetMap,
        int matchSize,
        int matchSetSize,
        Iterable<U> expectedUsers
    ) {
        boolean found = false;

        for (EnumMap<R, Object> ruleMap : this.matchResult.keySet())
            if (ruleSetMap.entrySet().stream().allMatch(entry -> {
                R ruleKey = entry.getKey();
                Set<?> ruleValueSet = entry.getValue();

                return ruleValueSet.contains(ruleMap.get(ruleKey));
            })) {
                found = true;

                Set<U> userSet = this.matchResult.get(ruleMap).stream()
                    .filter(match -> match.size() == matchSize)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

                assertThat(userSet)
                    .hasSize(matchSize * matchSetSize)
                    .isSubsetOf(expectedUsers);
            }

        assertThat(found)
            .isTrue();
    }

}

