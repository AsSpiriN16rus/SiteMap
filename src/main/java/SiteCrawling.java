import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.ListIterator;

public class SiteCrawling extends RecursiveAction
{

    private final String url;
    private static Set<Link> siteUrlList = new HashSet<>();
    private static Set<String> siteUrlListAfter = new HashSet<>();
    private int  nestingLevel = 0;

    public SiteCrawling(String url){
        this.url = url;
    }

    @Override
    protected void compute() {
        List<SiteCrawling> tasks =  new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).maxBodySize(0).get();
            Elements elements = document.select("a");
            TimeUnit.MILLISECONDS.sleep(120);
            for (Element em : elements){

                String absHref = em.attr("abs:href");
                int indexJava = absHref.indexOf(url);

                if (indexJava != -1 & !siteUrlListAfter.contains(absHref)){

                    Link link = new Link();
                    link.setUrl(absHref);
                    link.setNestingLevel(nestingLevel);
                    link.setUrlParents(url);

                    synchronized (siteUrlList) {
                        if (!siteUrlList.isEmpty()) {
                            for (Link link2 : siteUrlList) {
                                if (link.getUrlParents().equals(link2.getUrl())) {
                                    this.nestingLevel = link2.getNestingLevel() + 1;
                                    break;
                                }
                            }
                        }

                        link.setNestingLevel(nestingLevel);

                        siteUrlList.add(link);
                        siteUrlListAfter.add(absHref);
                    }


                    SiteCrawling task = new SiteCrawling(absHref);
                    task.fork();
                    tasks.add(task);
                }
            }

            for (SiteCrawling item : tasks)
            {
                item.join();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static String recursionFunc(Link link) {
        String recursionString = "";
        String recursionStringParents = "";

        recursionStringParents = link.getUrlParents();
        recursionString = link.getUrl();

        if (link.getNestingLevel() == 0){
            recursionString = link.getUrlParents() + "\n\t" + link.getUrl();
            return recursionString;
        }


        for (Link link1 : siteUrlList){
            if (link1.getUrl().equals(recursionStringParents)){
                if (link.getNestingLevel() == 1){
                    recursionString = recursionFunc(link1) + "\n\t\t" + recursionString;
                } else if (link.getNestingLevel() == 2) {
                    recursionString = recursionFunc(link1) + "\n\t\t\t" + recursionString;
                }

                break;
            }
        }

        return recursionString;
    }



    public static void fileRecording() {

        List<String> listUrl =  new ArrayList<>();

        for (Link link : siteUrlList){
            System.out.println("сслыка - " + link.getUrl()+ " уровень" + link.getNestingLevel());
            listUrl.add(recursionFunc(link));
        }

        try {
            Files.write(Paths.get("data/info.txt"),listUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
