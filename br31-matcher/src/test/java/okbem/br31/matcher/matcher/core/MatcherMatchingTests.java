
package okbem.br31.matcher.matcher.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.*;
import org.mockito.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * {@link Matcher}의 매치 기능을 검증하는 테스트.
 */
public class MatcherMatchingTests {

    private static enum Rule1 {
        COLOR,
        FOOD;

        private static enum Color { RED, GREEN, BLUE }
        private static enum Food { 피자, 치킨, 감자, 고구마 }
    }


    @Test
    public void testUseCase1() {
        TestMatcher<String, Rule1> matcher = new TestMatcher<>(
            Rule1.class,
            Duration.ofMillis(500L),
            new CountDownLatch(1),
            Duration.ofMillis(1000L)
        );

        EnumMap<Rule1, Set<?>> A = new EnumMap<>(Rule1.class);
        A.put(Rule1.COLOR, EnumSet.of(Rule1.Color.GREEN, Rule1.Color.BLUE));
        A.put(Rule1.FOOD, EnumSet.of(Rule1.Food.피자, Rule1.Food.고구마));

        EnumMap<Rule1, Set<?>> B = new EnumMap<>(Rule1.class);
        B.put(Rule1.COLOR, EnumSet.allOf(Rule1.Color.class));
        B.put(Rule1.FOOD, EnumSet.of(Rule1.Food.감자));

        EnumMap<Rule1, Set<?>> C = new EnumMap<>(Rule1.class);
        C.put(Rule1.COLOR, EnumSet.of(Rule1.Color.RED));
        C.put(Rule1.FOOD, EnumSet.allOf(Rule1.Food.class));

        matcher.submit("A", new HashSet<>(Arrays.asList(2)), A);
        matcher.submit("B", new HashSet<>(Arrays.asList(2, 3)), B);
        matcher.submit("C", new HashSet<>(Arrays.asList(3, 4)), C);

        assertThat(matcher.isMatched())
            .isFalse();
    }


    private static enum Rule2 {
        TOPIC,
        FOOD,
        GU,
        PLACE,
        TIME;

        private static enum Topic { 독서, 게임, 축구, 영화, 수다, 쇼핑 }
        private static enum Food { 피자, 치킨, 감자, 고구마, 커피, 소주, 맥주 }
        private static enum Gu { 강남, 종로, 마포, 서대문, 노원, 영등포 }
        private static enum Place { PC방, 도서관, 카페, 레스토랑, 야외 }
        private static enum Time { 아침, 점심, 저녁 }
    }


    @Test
    public void testUseCase2() {
        TestMatcher<String, Rule2> matcher = new TestMatcher<>(
            Rule2.class,
            Duration.ofMillis(500L),
            new CountDownLatch(4),
            Duration.ofMillis(1000L)
        );

        EnumMap<Rule2, Set<?>> A = new EnumMap<>(Rule2.class);
        A.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.게임, Rule2.Topic.영화
        ));
        A.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.피자, Rule2.Food.맥주
        ));
        A.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.종로, Rule2.Gu.마포, Rule2.Gu.서대문, Rule2.Gu.노원
        ));
        A.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.PC방, Rule2.Place.카페, Rule2.Place.레스토랑
        ));
        A.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.점심, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> B = new EnumMap<>(Rule2.class);
        B.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.독서, Rule2.Topic.영화, Rule2.Topic.수다
        ));
        B.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.피자, Rule2.Food.고구마, Rule2.Food.커피
        ));
        B.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.종로, Rule2.Gu.서대문, Rule2.Gu.노원
        ));
        B.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.도서관, Rule2.Place.카페, Rule2.Place.레스토랑
        ));
        B.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침, Rule2.Time.점심, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> C = new EnumMap<>(Rule2.class);
        C.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.독서, Rule2.Topic.게임, Rule2.Topic.쇼핑
        ));
        C.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.감자, Rule2.Food.고구마, Rule2.Food.커피
        ));
        C.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.강남, Rule2.Gu.종로, Rule2.Gu.서대문
        ));
        C.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.PC방, Rule2.Place.야외
        ));
        C.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침, Rule2.Time.점심, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> D = new EnumMap<>(Rule2.class);
        D.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.게임
        ));
        D.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.커피
        ));
        D.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.강남, Rule2.Gu.종로, Rule2.Gu.영등포
        ));
        D.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.PC방
        ));
        D.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침, Rule2.Time.점심, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> E = new EnumMap<>(Rule2.class);
        E.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.게임, Rule2.Topic.축구, Rule2.Topic.수다
        ));
        E.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.커피, Rule2.Food.소주, Rule2.Food.맥주
        ));
        E.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.강남, Rule2.Gu.종로, Rule2.Gu.서대문, Rule2.Gu.영등포
        ));
        E.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.PC방, Rule2.Place.도서관, Rule2.Place.야외
        ));
        E.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> F = new EnumMap<>(Rule2.class);
        F.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.축구, Rule2.Topic.쇼핑, Rule2.Topic.독서
        ));
        F.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.치킨, Rule2.Food.감자, Rule2.Food.소주
        ));
        F.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.노원
        ));
        F.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.카페, Rule2.Place.레스토랑, Rule2.Place.야외
        ));
        F.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침
        ));

        EnumMap<Rule2, Set<?>> G = new EnumMap<>(Rule2.class);
        G.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.쇼핑, Rule2.Topic.독서, Rule2.Topic.수다
        ));
        G.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.치킨, Rule2.Food.감자
        ));
        G.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.노원
        ));
        G.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.카페, Rule2.Place.레스토랑
        ));
        G.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침
        ));

        EnumMap<Rule2, Set<?>> H = new EnumMap<>(Rule2.class);
        H.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.쇼핑, Rule2.Topic.독서
        ));
        H.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.치킨, Rule2.Food.감자, Rule2.Food.맥주
        ));
        H.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.노원
        ));
        H.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.카페, Rule2.Place.레스토랑, Rule2.Place.PC방
        ));
        H.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> I = new EnumMap<>(Rule2.class);
        I.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.쇼핑, Rule2.Topic.게임, Rule2.Topic.독서
        ));
        I.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.치킨, Rule2.Food.감자
        ));
        I.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.노원, Rule2.Gu.마포
        ));
        I.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.카페, Rule2.Place.레스토랑, Rule2.Place.도서관
        ));
        I.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침, Rule2.Time.점심
        ));

        EnumMap<Rule2, Set<?>> ruleSetMap1 = new EnumMap<>(Rule2.class);
        ruleSetMap1.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.영화
        ));
        ruleSetMap1.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.피자
        ));
        ruleSetMap1.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.종로, Rule2.Gu.서대문, Rule2.Gu.노원
        ));
        ruleSetMap1.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.카페, Rule2.Place.레스토랑
        ));
        ruleSetMap1.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.점심, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> ruleSetMap2 = new EnumMap<>(Rule2.class);
        ruleSetMap2.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.게임
        ));
        ruleSetMap2.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.커피
        ));
        ruleSetMap2.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.강남, Rule2.Gu.종로
        ));
        ruleSetMap2.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.PC방
        ));
        ruleSetMap2.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침, Rule2.Time.저녁
        ));

        EnumMap<Rule2, Set<?>> ruleSetMap3 = new EnumMap<>(Rule2.class);
        ruleSetMap3.put(Rule2.TOPIC, EnumSet.of(
            Rule2.Topic.독서, Rule2.Topic.쇼핑
        ));
        ruleSetMap3.put(Rule2.FOOD, EnumSet.of(
            Rule2.Food.치킨, Rule2.Food.감자
        ));
        ruleSetMap3.put(Rule2.GU, EnumSet.of(
            Rule2.Gu.노원
        ));
        ruleSetMap3.put(Rule2.PLACE, EnumSet.of(
            Rule2.Place.카페, Rule2.Place.레스토랑
        ));
        ruleSetMap3.put(Rule2.TIME, EnumSet.of(
            Rule2.Time.아침
        ));

        matcher.submit("A", new HashSet<>(Arrays.asList(2, 3, 4, 5)), A);
        matcher.submit("B", new HashSet<>(Arrays.asList(2)), B);
        matcher.submit("C", new HashSet<>(Arrays.asList(2, 3, 4)), C);
        matcher.submit("D", new HashSet<>(Arrays.asList(3, 5, 7)), D);
        matcher.submit("E", new HashSet<>(Arrays.asList(3, 4)), E);
        matcher.submit("F", new HashSet<>(Arrays.asList(2, 3, 4, 5)), F);
        matcher.submit("G", new HashSet<>(Arrays.asList(2, 4, 5, 6, 7)), G);
        matcher.submit("H", new HashSet<>(Arrays.asList(2, 3)), H);
        matcher.submit("I", new HashSet<>(Arrays.asList(2)), I);

        assertThat(matcher.isMatched())
            .isTrue();

        assertThat(matcher.matchResult)
            .hasSize(3);

        matcher.assertMatch(ruleSetMap1, 2, 1, "A", "B");
        matcher.assertMatch(ruleSetMap2, 3, 1, "C", "D", "E");
        matcher.assertMatch(ruleSetMap3, 2, 2, "F", "G", "H", "I");
    }


    private static enum Rule3 {
    }


    @Test
    public void testUseCase3() {
        TestMatcher<String, Rule3> matcher = new TestMatcher<>(
            Rule3.class,
            Duration.ofMillis(1500L),
            new CountDownLatch(3),
            Duration.ofMillis(2000L)
        );

        EnumMap<Rule3, Set<?>> ruleSetMap = new EnumMap<>(Rule3.class);

        matcher.submit("A", new HashSet<>(Arrays.asList(2)));
        matcher.submit("B", new HashSet<>(Arrays.asList(2)));
        matcher.submit("C", new HashSet<>(Arrays.asList(2)));
        matcher.submit("D", new HashSet<>(Arrays.asList(3)));

        assertThat(matcher.isMatched())
            .isFalse();

        matcher.assertMatch(ruleSetMap, 2, 1, "A", "B", "C");

        matcher.cancel("D");
        matcher.submit("D", new HashSet<>(Arrays.asList(2)));
        matcher.submit("E", new HashSet<>(Arrays.asList(2)));
        matcher.submit("F", new HashSet<>(Arrays.asList(2)));

        assertThat(matcher.isMatched())
            .isTrue();

        matcher.assertMatch(ruleSetMap, 2, 3, "A", "B", "C", "D", "E", "F");
    }


    private static enum Rule4 {
    }


    @Test
    public void testUseCase4() {
        TestMatcher<String, Rule4> matcher = new TestMatcher<>(
            Rule4.class,
            Duration.ofMillis(500L),
            new CountDownLatch(2),
            Duration.ofMillis(1000L)
        );

        String[] users = String.join("", Arrays.asList(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
            "abcdefghijklmnopqrstuvwxyz"
        )).split("");

        for (String user : users)
            matcher.submit(user, new HashSet<>(Arrays.asList(26)));

        assertThat(matcher.isMatched())
            .isTrue();

        matcher.assertMatch(new EnumMap<>(Rule4.class), 26, 2, users);
    }

}

