# Как работает Circuit Breaker

## Концепция Circuit Breaker

Circuit Breaker (автоматический выключатель) - это паттерн проектирования для защиты приложения от каскадных сбоев при недоступности внешних сервисов.

### Три состояния Circuit Breaker:

1. **CLOSED (Закрыт)** - нормальная работа
   - Все запросы проходят к внешнему сервису
   - Отслеживаются успешные и неуспешные вызовы
   - Если процент ошибок превышает порог → переход в OPEN

2. **OPEN (Открыт)** - защитный режим
   - Все запросы **сразу** возвращаются через fallback
   - НЕ делаются реальные вызовы к внешнему сервису
   - Через определенное время (waitDurationInOpenState) → переход в HALF_OPEN

3. **HALF_OPEN (Полуоткрыт)** - тестовый режим
   - Разрешается ограниченное количество запросов (permittedNumberOfCallsInHalfOpenState)
   - Если все успешны → переход в CLOSED
   - Если есть ошибки → возврат в OPEN

## Как это работает в вашем приложении

### У вас ДВА уровня Circuit Breaker:

#### 1. Circuit Breaker на уровне Feign клиента (автоматический)

```yaml
feign:
  circuitbreaker:
    enabled: true  # Включает Circuit Breaker для всех Feign клиентов
```

**Как работает:**
- Когда book-service недоступен, Feign выбрасывает исключение (ConnectException)
- Circuit Breaker перехватывает это исключение
- Если процент ошибок > 50% → Circuit Breaker открывается
- Все последующие вызовы идут в `BookServiceFallback` класс
- Fallback возвращает BookResponse со status "UNKNOWN"

**Важно:** Circuit Breaker на уровне Feign работает автоматически, но нужно правильно настроить его параметры.

#### 2. Circuit Breaker на уровне методов сервиса (через аннотации)

```java
@CircuitBreaker(name = "bookService", fallbackMethod = "createFallback")
public ExchangeRequestResponse create(...) {
    // ...
}
```

**Как работает:**
- Аннотация `@CircuitBreaker` оборачивает метод в Circuit Breaker
- При ошибке вызывается fallback-метод
- Но это работает только если исключение не перехватывается раньше

## Проблема в текущей реализации

У вас есть **конфликт** между двумя уровнями:

1. **Feign Circuit Breaker** перехватывает ошибки и вызывает `BookServiceFallback`
2. **Метод Circuit Breaker** (`@CircuitBreaker`) не срабатывает, потому что исключение уже обработано

## Правильная настройка

### Вариант 1: Только Feign Circuit Breaker (рекомендуется)

1. Убрать аннотации `@CircuitBreaker` с методов сервиса
2. Полагаться только на Feign Circuit Breaker + fallback класс
3. Обрабатывать результат из fallback (status "UNKNOWN")

### Вариант 2: Только методный Circuit Breaker

1. Отключить Circuit Breaker для Feign: `feign.circuitbreaker.enabled: false`
2. Убрать fallback класс из `@FeignClient`
3. Использовать только `@CircuitBreaker` на методах

### Вариант 3: Комбинированный (текущий, но нужно исправить)

1. Feign Circuit Breaker обрабатывает сетевые ошибки
2. Методный Circuit Breaker обрабатывает бизнес-логику
3. Нужно правильно обрабатывать результаты fallback

## Параметры Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      bookService:
        minimumNumberOfCalls: 1        # Минимум вызовов перед оценкой
        slidingWindowSize: 10          # Размер окна для оценки
        failureRateThreshold: 50       # Порог ошибок (50%)
        waitDurationInOpenState: 5s   # Время в OPEN перед переходом в HALF_OPEN
```

**Что происходит:**
1. После 1 неудачного вызова (minimumNumberOfCalls: 1)
2. Если ошибок > 50% из последних 10 вызовов (slidingWindowSize: 10, failureRateThreshold: 50)
3. Circuit Breaker открывается
4. Все запросы идут в fallback
5. Через 5 секунд переход в HALF_OPEN для проверки восстановления

## Рекомендации

1. **Используйте только Feign Circuit Breaker** - он проще и работает автоматически
2. **Настройте таймауты** - чтобы быстро обнаруживать недоступность сервиса
3. **Обрабатывайте fallback результаты** - проверяйте status "UNKNOWN" и выбрасывайте правильные исключения
4. **Логируйте активацию Circuit Breaker** - чтобы видеть, когда он срабатывает

