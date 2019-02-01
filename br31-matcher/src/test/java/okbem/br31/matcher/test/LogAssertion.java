
package okbem.br31.matcher.test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.Appender;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Assertion methods for SLF4J logging.
 * SLF4J로 남기는 로그를 검증하기 위한 유틸리티 메서드를 제공한다.
 *
 * 검증의 대상이 되는 {@link Logger}에 {@link Appender}를 추가하여
 * {@link Appender#doAppend(Object)} 호출을 캡쳐하는 방식으로 작동한다.
 *
 * 동일한 {@code LogAssertion} 인스턴스를 여러 개의 테스트 케이스에서 재사용하면
 * 안 된다. 하나의 인스턴스는 반드시 하나의 테스트 케이스에서만 사용해야 하며,
 * 여러 개의 테스트 케이스에서 사용해야 할 경우에는 각 테스트 케이스마다 별도의
 * 인스턴스를 생성해서 사용해야 한다.
 */
public class LogAssertion {

    /**
     * SLF4J로 남기는 로그를 캡쳐하기 위한 mock {@link Appender}.
     */
    @SuppressWarnings("unchecked")
    protected final Appender<ILoggingEvent> mockAppender = mock(Appender.class);


    /**
     * 검증의 대상이 되는 {@link Logger}의 이름.
     */
    protected final String loggerName;


    /**
     * {@code LogAssertion} 인스턴스를 생성한다.
     *
     * @param loggerName 검증의 대상이 되는 {@link Logger}를 지정하기 위한
     *                   클래스 타입
     */
    public LogAssertion(Class<?> loggerName) {
        Logger logger = (Logger)LoggerFactory.getLogger(loggerName);

        this.loggerName = logger.getName();

        logger.addAppender(this.mockAppender);
    }


    /**
     * {@code LogAssertion} 인스턴스를 생성한다.
     *
     * @param loggerName 검증의 대상이 되는 {@link Logger}의 이름
     */
    public LogAssertion(String loggerName) {
        Logger logger = (Logger)LoggerFactory.getLogger(loggerName);

        this.loggerName = logger.getName();

        logger.addAppender(this.mockAppender);
    }


    /**
     * {@code LogAssertion} 인스턴스가 생성된 이후부터 지금까지 캡쳐한 모든
     * 로그를 리턴한다.
     *
     * @return 캡쳐한 로그의 목록
     */
    public final List<ILoggingEvent> getAllLogs() {
        ArgumentCaptor<ILoggingEvent> captor
            = ArgumentCaptor.forClass(ILoggingEvent.class);

        verify(this.mockAppender, atLeast(0))
            .doAppend(captor.capture());

        return captor.getAllValues();
    }


    /**
     * 특정 로그가 남겨졌는지 검증한다.
     *
     * @param logLevel 로그 레벨
     * @param logMessage 로그 메시지
     */
    public final void assertLog(String logLevel, String logMessage) {
        this.assertLog(logLevel, logMessage, null, null);
    }


    /**
     * 특정 로그가 예외와 함께 남겨졌는지 검증한다.
     *
     * @param logLevel 로그 레벨
     * @param logMessage 로그 메시지
     * @param exceptionType 예외 클래스 타입
     * @param exceptionMessage 예외 클래스에 담겨 있는 메시지
     */
    public final void assertLog(
        String logLevel,
        String logMessage,
        Class<? extends Throwable> exceptionType,
        String exceptionMessage
    ) {
        // 캡쳐한 로그 중에서 조건을 만족하는 로그가 하나라도 있는지 검증한다.
        assertThat(this.getAllLogs()).anySatisfy(event -> {
            // {@link Logger}의 이름이 일치하는지 검증한다.
            // 하위 {@link Logger}가 존재할 수 있으므로 반드시 필요한 작업이다.
            assertThat(event.getLoggerName())
                .isEqualTo(this.loggerName);

            // 로그 레벨이 일치하는지 검증한다.
            assertThat(event.getLevel().toString())
                .isEqualTo(logLevel);

            // 로그 메시지가 일치하는지 검증한다.
            assertThat(event.getFormattedMessage())
                .isEqualTo(logMessage);

            ThrowableProxy proxy = (ThrowableProxy)event.getThrowableProxy();

            if (exceptionType != null) {
                // {@code exceptionType}에 특정 예외 클래스 타입이 지정되었다면
                // 로그에 예외가 포함되어 있는지 검증한다.
                assertThat(proxy)
                    .isNotNull();

                // 포함된 예외의 클래스 타입과 담겨 있는 메시지를 검증한다.
                assertThat(proxy.getThrowable())
                    .isInstanceOf(exceptionType)
                    .hasMessage(exceptionMessage);
            } else {
                // {@code exceptionType}에 지정된 예외 클래스 타입이 없다면
                // 로그에 예외가 포함되어 있지 않은지 검증한다.
                assertThat(proxy)
                    .isNull();
            }
        });
    }

}

