import java.util.concurrent.ForkJoinPool;

public class Main {

    public static void main(String[] args) {
            String url = "https://lenta.ru";
            ForkJoinPool fjp = new ForkJoinPool();
            fjp.invoke(new SiteCrawling(url));
            SiteCrawling.fileRecording();

    }

}
