package packege;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.*;

@NoArgsConstructor
public class CrptApi {
    @Getter
    private AtomicReference<TimeUnit> timeUnit = new AtomicReference<>();
    @Getter
    private AtomicInteger requestLimit = new AtomicInteger();
    private RateLimiter limiter;
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit.set(timeUnit);
        this.requestLimit.set(requestLimit);
        limiter = RateLimiter.create((double) requestLimit, 1, timeUnit);
    }

    public void saveDoc (JSOnDocument document, String signature)  {
        limiter.acquire();

        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();

            mapper.writeValue(writer, document);
            System.out.println("document: " + "\n" + writer.toString());
            postJsonDocument(writer.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void postJsonDocument(String value){
        String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(value))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit.set(timeUnit);
    }

    public void setRequestLimit(int requestLimit) {
        this.requestLimit.set(requestLimit);
    }

    @Data
    @AllArgsConstructor
    @JsonAutoDetect
    static class JSOnDocument{
        private Description description;
        private String doc_id;
        private String doc_status;
        private DocType doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private Date production_date;
        private String production_type;
        private List<Products> products;
        private Date reg_date;
        private String reg_number;
    }

    @Data
    @AllArgsConstructor
    @JsonAutoDetect
    static class Products{
        private String certificate_document;
        private Date certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private Date production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    @AllArgsConstructor
    @JsonAutoDetect
    static class Description{
        @Setter
        @Getter
        private String participantInn;
    }
    @JsonAutoDetect
    static enum DocType{
        LP_INTRODUCE_GOODS
    }

}





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