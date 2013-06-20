import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.*;
import java.io.File;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

public class XangaScrape{
    String DIR = "posts";
    HtmlPage page;
    String username, password;

    public XangaScrape(String user, String pw){
        username = user;
        password = pw;
    }

    public HtmlAnchor getNextAnchor(){
        for (HtmlAnchor a: page.getAnchors()){
            if (a.getHrefAttribute().indexOf("direction=n") >= 0){
                return a;
            }
        }
        return null;
    }

    public ArrayList<HtmlAnchor> getCommentAnchors(){
        ArrayList<HtmlAnchor> anchors = new ArrayList<HtmlAnchor>();
        Pattern p = Pattern.compile(".*" + username + ".xanga.com/(\\d+)/.+");
        // To keep track of dupes
        ArrayList<String> urllist = new ArrayList<String>();
        for (HtmlAnchor a: page.getAnchors()){
            String url = a.getHrefAttribute();
            if (p.matcher(url).matches() && !urllist.contains(url)){
                anchors.add(a);
                urllist.add(url);
            }
        }
        return anchors;
    }

    public void run(){
        // Remove warnings
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        WebClient b = new WebClient();
        CookieManager cm = b.getCookieManager();
        cm.setCookiesEnabled(true);
        b.setJavaScriptEnabled(true);

        // Yes I'm putting everything in try. don't yell at me :)
        try{
            // LOGIN
            if (password.equals("")){
                page = b.getPage("http://" + username + ".xanga.com");

            } else{
                page = b.getPage("http://www.xanga.com");
                ((HtmlTextInput)page.getElementById("XangaHeader_txtSigninUsername")).setValueAttribute(username);
                ((HtmlPasswordInput)page.getElementById("XangaHeader_txtSigninPassword")).setValueAttribute(password);
                page = ((HtmlAnchor)page.getElementById("signin")).click();
            }


            b.setJavaScriptEnabled(false);

            boolean success = new File(DIR).mkdirs();
            if (!success){
                System.err.println("Cannot create dir: " + DIR + ". Maybe dir already exists?");
            }


            HtmlAnchor anchor = getNextAnchor();
            int pagecount = 1;
            while (pagecount == 1 || anchor != null){
                String filename = DIR + "/page" + pagecount + ".html";
                File f = new File(filename);
                page.save(f);
                System.out.println("Saved: " + filename);

                int commentcount = 1;
                // Get comments
                for (HtmlAnchor a: getCommentAnchors()){
                    HtmlPage commentPage = a.click();
                    Pattern p = Pattern.compile(".*" + username + ".xanga.com/(\\d+)/.+");
                    Matcher m = p.matcher(a.getHrefAttribute());
                    if (m.find()){
                        String comment_id = m.group(1);
                        String comment_filename = DIR + "/page" + pagecount + "/" + comment_id + ".html";
                        File f2 = new File(comment_filename);

                        System.out.println("    Saved comment: " + comment_filename);
                        commentPage.save(f2);
                    } 
                }

                pagecount++;
                page = anchor.click();
                anchor = getNextAnchor();
            }

        } catch (Exception e){
            e.printStackTrace();

            System.out.println("\nOops! Something went wrong. It's probably your fault");
        }
    }

    public void fixLinks(){
        Runtime r = Runtime.getRuntime();
        try{
            Process p = r.exec("bash fixlinks.sh " + username);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter username:");
        String username = scan.nextLine();
        System.out.println("Enter password: (This is optional. If you leave this blank, then posts that cannot be seen from the public will not be downloaded.");
        String pw = scan.nextLine();
        
        XangaScrape t = new XangaScrape(username,pw);
        t.run();
        t.fixLinks();
    }
}
