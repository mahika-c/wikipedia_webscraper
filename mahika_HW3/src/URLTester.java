// import java.net.URL;
import java.util.ArrayList;
public class URLTester {
    public static void main(String[] args) {
        URLGetter url = new URLGetter("http://www.upenn.edu");
        System.out.println(url.getRedirectURL());
        url.printStatusCode();
        ArrayList<String> page = url.getContents();
        for (String line : page) {
            System.out.println(line);
        }
    }
}