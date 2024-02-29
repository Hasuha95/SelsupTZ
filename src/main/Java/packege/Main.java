package packege;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Getter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    static RateLimiter limiter = RateLimiter.create(10);

    public static void main(String[] args) throws InterruptedException {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 10);


        CrptApi.Description description = new CrptApi.Description("participantInn");
        CrptApi.Products product = new CrptApi.Products("certificate_document",
                Date.valueOf(LocalDate.now()),
                "certificate_document_number",
                "owner_inn",
                "producer_inn",
                Date.valueOf(LocalDate.now()),
                "tnved_code",
                "uit_code",
                "uitu_code");

        List<CrptApi.Products> list = new ArrayList<>(Arrays.asList(product));
        CrptApi.JSOnDocument doc = new CrptApi.JSOnDocument(description,
                "doc_id",
                "doc_status",
                CrptApi.DocType.LP_INTRODUCE_GOODS,
                true,
                "owner_inn",
                "participian_inn",
                Date.valueOf(LocalDate.now()),
                "production_type",
                list,
                Date.valueOf(LocalDate.now()),
                "reg_number");


        api.saveDoc(doc, "signature");

//        long startSeconds = System.currentTimeMillis();
//        Thread thread = null;
//        for (int i = 0; i < 100; i++) {
////            thread = new Thread(() -> api.saveDoc());
//            thread.start();
//        }
//        thread.join();
//        long timer = System.currentTimeMillis() - startSeconds;
//        System.out.println(timer);

    }



    public static void method() {
        try {
            limiter.acquire();
            Thread.sleep(1000);
            System.out.println("method done");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
