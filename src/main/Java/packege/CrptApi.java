package packege;

/*
Необходимо реализовать на языке Java (можно использовать 17 версию) класс для работы с API Честного знака.
Класс должен быть thread-safe и поддерживать ограничение на количество запросов к API.
Ограничение указывается в конструкторе в виде количества запросов в определенный интервал времени.
Например:
public CrptApi(TimeUnit timeUnit, int requestLimit)
timeUnit – указывает промежуток времени – секунда, минута и пр.
requestLimit – положительное значение, которое определяет максимальное количество запросов в этом промежутке времени.

При превышении лимита запрос вызов должен блокироваться, чтобы не превысить максимальное количество запросов к API
и продолжить выполнение, без выбрасывания исключения, когда ограничение на количество вызов API не будет превышено
в результате этого вызова. В любой ситуации превышать лимит на количество запросов запрещено для метода.

Реализовать нужно единственный метод – Создание документа для ввода в оборот товара, произведенного в РФ.
Документ и подпись должны передаваться в метод в виде Java объекта и строки соответственно.

Вызывается по HTTPS метод POST следующий URL:
https://ismp.crpt.ru/api/v3/lk/documents/create

В теле запроса передается в формате JSON документ:
{
"description":
    {
    "participantInn": "string"
    },
"doc_id": "string",
"doc_status": "string",
"doc_type": "LP_INTRODUCE_GOODS",
"importRequest": true,
"owner_inn": "string",
"participant_inn": "string",
"producer_inn": "string",
"production_date": "2020-01-23",
"production_type": "string",
"products": [
    {
    "certificate_document": "string",
    "certificate_document_date": "2020-01-23",
    "certificate_document_number": "string",
    "owner_inn": "string",
    "producer_inn": "string",
    "production_date": "2020-01-23",
    "tnved_code": "string",
    "uit_code": "string",
    "uitu_code": "string"
    } ],
"reg_date": "2020-01-23",
"reg_number": "string"
}

При реализации можно использовать библиотеки HTTP клиента, JSON сериализации.
Реализация должна быть максимально удобной для последующего расширения функционала.

Решение должно быть оформлено в виде одного файла CrptApi.java.
Все дополнительные классы, которые используются должны быть внутренними.

Можно прислать ссылку на файл в GitHub.
В задании необходимо просто сделать вызов указанного метода,
реальный API не должен интересовать.

 */

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.*;

@NoArgsConstructor
public class CrptApi {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String URI_EXTERNAL_SERVICE = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient client = HttpClient.newHttpClient();
    @Getter
    private AtomicReference<TimeUnit> timeUnit = new AtomicReference<>();
    @Getter
    private AtomicInteger requestLimit = new AtomicInteger();
    private RateLimiter limiter;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit.set(timeUnit);
        this.requestLimit.set(requestLimit);
        limiter = RateLimiter.create(requestLimit, 1, timeUnit);
    }

    /**
     * Выполнение POST-запроса на создание документа
     *
     * @param signature подпись
     * @param document документ
     * @return void
     */
    public HttpResponse<String> saveDoc(@NonNull final JSONDocument document,
                                        @NonNull final String signature) {
        limiter.acquire();
        final StringWriter writer = new StringWriter();
        try {
            MAPPER.writeValue(writer, document);
            HttpRequest request = HttpRequest.newBuilder(URI.create(URI_EXTERNAL_SERVICE))
                    .POST(HttpRequest.BodyPublishers.ofString(writer.toString()))
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit.set(timeUnit);
    }

    public void setRequestLimit(int requestLimit) {
        this.requestLimit.set(requestLimit);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect
class JSONDocument {
    @JsonProperty("description")
    private Description description;
    @JsonProperty("doc_id")
    private String docId;
    @JsonProperty("doc_status")
    private String docStatus;
    @JsonProperty("doc_type")
    private DocType docType;
    @JsonProperty("importRequest")
    private boolean importRequest;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("participant_inn")
    private String participantInn;
    @JsonProperty("production_date")
    private Date productionDate;
    @JsonProperty("production_type")
    private String productionType;
    @JsonProperty("products")
    private List<Products> products;
    @JsonProperty("reg_date")
    private Date regDate;
    @JsonProperty("reg_number")
    private String regNumber;
}

@Data
@AllArgsConstructor
@JsonAutoDetect
class Products {
    @JsonProperty("certificate_document")
    private String certificateDocument;
    @JsonProperty("certificate_document_date")
    private Date certificateDocumentDate;
    @JsonProperty("certificate_document_number")
    private String certificateDocumentNumber;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("producer_inn")
    private String producerInn;
    @JsonProperty("production_date")
    private Date productionDate;
    @JsonProperty("tnved_code")
    private String tnvedCode;
    @JsonProperty("uit_code")
    private String uitCode;
    @JsonProperty("uitu_code")
    private String uituCode;
}

@AllArgsConstructor
@JsonAutoDetect
class Description {
    @Setter
    @Getter
    @JsonProperty("participantInn")
    private String participantInn;
}
@JsonAutoDetect
enum DocType {
    LP_INTRODUCE_GOODS
}