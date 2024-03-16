import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Class that loads a document given a URL and has one method to answer
 * each trivia style question on the homework. Uses the JSoup library
 * to parse the HTML and regex when needed.
 * Author: Mahika Calyanakoti
 * Class: NETS 1500
 * Date: 4/4/2023
 */
public class WebScraper {
    // stores the basic part of the URL
    private String baseURL;
    // stores the document to be parsed through in the webscraping
    private Document currentDoc;

    // maps school to frequency of nominations
    private HashMap<String, Integer> schoolToCount;

    // maps country to frequency of nominations
    private HashMap<String, Integer> countryToCount;

    // sum of nominees' birth years to calculate average for Q8
    private int birthYearSum;

    // sum of nominees' ages to calculate average for Q8
    private int ageCount;

    /**
     * Constructor that connects to an input url and loads the document
     * to the private instance variable currentdoc.
     * @param url The URL from which the document should be fetched.
     */
    public WebScraper(String url) {
        try {
            currentDoc = Jsoup.connect(url).get();
            String[] sections = url.split("/");
            baseURL = "https://" + sections[2];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Goes to the discontinued categories section and extracts the text parts
     * of each line, which each correspond to an award category.
     * Uses regex to accomplish this extraction.
     */
    public void discontinuedCategories() {
        Element discontinuedCategories = currentDoc.select("div.div-col").first();
        Element ul = discontinuedCategories.getElementsByTag("ul").first();
        // finds all "li" tags
        Elements categories = ul.children();
        for (Element e : categories) {
            String line = e.select("li").text();
            // retrieves the text part that comes before the ":"
            // aka the award name
            String template = "(.*):.*";

            // regex
            Pattern p = Pattern.compile(template);
            Matcher m = p.matcher(line);
            if (m.find()) {
                System.out.println(m.group(1));
            }
        }
    }

    /**
     * Accesses the Awards of Merit categories by checking the caption of each table.
     * Then iterates through each row and stores the years and awards in a HashMap.
     * Iterates through the HashMap and prints an award category if its year matches
     * the input year.
     * @param decade User inputs a 4-digit year that ends in a 0 to indicate which decade
     *               of award creation they want. I use the first 3 digits to identify the
     *               desired decade.
     */
    public void addedCategories(String decade) {
        // finds all tables in the document
        Elements tables = currentDoc.select("table");
        // maps award to year
        HashMap<String, String> awardCategories = new HashMap<>();
        // identify the exact table we want by checking caption
        for (Element e : tables) {
            String caption = e.children().first().text();
            if (caption.contains("Awards of Merit categories")) {
                // gets each row of the table
                Elements entries = e.getElementsByTag("tbody").first().children();

                for (Element ent : entries) {
                    String text = ent.text();
                    String year = text.substring(0, 4); // first 3 characters correspond to decade
                    if (!year.equals("Year")) { // first row is header
                        // gets award name
                        String award = ent.getElementsByTag("td").select("a").text();
                        // add to the hashmap
                        awardCategories.put(award, year);
                    }
                }

                // decade is only concerned with the first 3 digits of the year
                String query = decade.substring(0, 3);

                for (String award : awardCategories.keySet()) {
                    // print award category if matches with the decade
                    if (awardCategories.get(award).substring(0, 3).equals(query)) {
                        System.out.println(award);
                    }
                }
                break;
            }
        }
    }

    /**
     * Follows a link corresponding the Academy Awards held in that year, and then counts
     * up the number of nominations for each of the movies. Lastly, returns all movie names
     * that have been nominated a minimum number of times in that year.
     * @param minimum The minimum number of award the movie should be nominated for.
     * @param year The year in which the movies were made.
     */
    public void miminumNominations(String minimum, String year) {
        // goes to that year's movies' academy awards
        Document yearDoc = getYearDocument(year);

        // maps movie titles to numbers of nominations
        HashMap<String, Integer> numNominations = new HashMap<>();

        // reasonable assumption that the first table on any academy awards page
        // is the list of awards/nominations, which is the case for all of them
        Element table = yearDoc.select("table.wikitable").first();
        Element tbody = table.getElementsByTag("tbody").first();

        // access the rows of the table
        Elements rows = tbody.getElementsByTag("tr");

        for (Element row : rows) {
            Elements bulletBoxes = row.getElementsByTag("td");
            for (Element box : bulletBoxes) {
                // all movie names are under "li"s in the table
                Element ul = box.getElementsByTag("ul").first();
                if (ul != null) {
                    for (Element li : ul.children()) {
                        Element ul2 = li.getElementsByTag("ul").first();
                        if (ul2 != null) {
                            for (Element li2 : ul.children()) {
                                Elements movieTitles2 = li2.getElementsByTag("i").select("a");
                                // once you access a movie title, increment the counter in hashmap
                                for (Element title : movieTitles2) {
                                    // System.out.println(title.text());
                                    numNominations.merge(title.text(), 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("The movies nominated at least " + minimum + " times are:");
        for (String movie : numNominations.keySet()) {
            // filter based on reaching the minimum number of desired nominations
            if (numNominations.get(movie).compareTo(Integer.parseInt(minimum)) > -1) {
                System.out.println(movie);
            }
        }
    }

    /**
     * Helper method used for Q3 and Q4 to retrieve the document corresponding
     * to the Award Ceremony for a certain year by following links in the input URL.
     * @param year The desired year in which the movies were made (ceremony held in the next year)
     * @return A Document retrieved by connecting to URL of Academy Awards ceremony for that year.
     */
    public Document getYearDocument(String year) {
        // year to link
        HashMap<String, String> linkMap = new HashMap<>();

        // access the table that lists all the years and the url appending strings
        Elements tables = currentDoc.select("div.navbox");
        Element table = tables.attr("aria-labelledby", "Academy-Awards").first();
        Elements trs = table.getElementsByTag("tbody").first().children();
        for (Element tr : trs) {
            Element th = tr.getElementsByTag("th").first();
            if (th != null) {
                // select the table that lists out the ceremonies links
                if (th.select("a").text().contains("Ceremonies")) {
                    Element td = tr.getElementsByTag("td").first();
                    Element div = td.getElementsByTag("div").first();
                    Elements lis = div.getElementsByTag("ul").first().children();
                    // the first 4 digits are the year, and the href directs you to the link
                    for (Element li : lis) {
                        linkMap.put(li.text().substring(0, 4), li.select("a").first().attr("href"));
                    }
                }
            }
        }

        // for (String y : linkMap.keySet()) {
        //     System.out.println(y + " " + linkMap.get(y));
        // }

        // if the year doesn't exist
        if (linkMap.get(year) == null) {
            System.out.println("That year's academy awards page could not be found.");
        }

        try {
            // fetch the document by appending the url ending to the base url
            String link = baseURL + linkMap.get(year);
            // System.out.println(link);
            return Jsoup.connect(link).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds the winning movie for a certain award title in a given year, using the helper method
     * to fetch the doc for that year.
     * @param awardName The name of the award user wants to find the winner for. Accounts for case
     *                  but not spelling typos.
     * @param year The year in which the movie was made.
     */
    public void winningFilm(String awardName, String year) {
        // get the year's webpage and access the awards table
        Document yearDoc = getYearDocument(year);
        Element table = yearDoc.select("table.wikitable").first();
        Element tbody = table.getElementsByTag("tbody").first();
        Elements rows = tbody.getElementsByTag("tr");

        // flag to determine if the award name was found or not
        boolean flag = false;
        // access each movie title in the award table

        // format 1: each box title has the bullets under it
        if (rows.first().getElementsByTag("td").first() != null) {
            for (Element row : rows) {
                Elements bulletBoxes = row.getElementsByTag("td");
                for (Element box : bulletBoxes) {
                    Element div = box.getElementsByTag("div").first();

                    Element b = div.getElementsByTag("b").first();
                    String awardTitle = b.select("a").text();
                    Element ul = box.getElementsByTag("ul").first();
                    if (ul != null) {
                        for (Element li : ul.children()) {
                            Element ul2 = li.getElementsByTag("ul").first();
                            if (ul2 != null) {
                                for (Element li2 : ul.children()) {
                                    Element title = li2.getElementsByTag("i").select("a").first();

                                    // if award name found, print it out/switch the flag to "found"
                                    if (awardTitle.toLowerCase().equals(awardName.toLowerCase())) {
                                        System.out.println(title.text());
                                        flag = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else { // format 2: each row lists 2 award categories, next row has 2 boxes of bullets
            for (Element row : rows) {
                Elements headers = row.getElementsByTag("th");
                for (Element header : headers) {
                    String awardTitle = header.select("a").text();
                    // System.out.println(awardTitle);
                    if (awardTitle.equalsIgnoreCase(awardName)) {
                        flag = true;
                        // film winner
                        Element bullet = header.parent().nextElementSibling();
                        if (header.equals(headers.first())) { // left column
                            Element firstTD = bullet.getElementsByTag("td").first();
                            Element firstUL = firstTD.getElementsByTag("ul").first();
                            Element firstLI = firstUL.getElementsByTag("li").first();
                            Element firstB = firstLI.getElementsByTag("b").first();
                            String movie = firstB.getElementsByTag("i").first().select("a").text();
                            System.out.println("\n" + movie);
                        } else { // right column
                            Element firstTD = bullet.getElementsByTag("td").get(1);
                            Element firstUL = firstTD.getElementsByTag("ul").first();
                            Element firstLI = firstUL.getElementsByTag("li").first();
                            Element firstB = firstLI.getElementsByTag("b").first();
                            String movie = firstB.getElementsByTag("i").first().select("a").text();
                            System.out.println("\n" + movie);
                        }
                    }
                }
            }
        }


        // if award not found, return invalid input
        if (!flag) {
            System.out.println("Invalid input");
        }
    }

    /**
     * Prints the budget and box office earnings of a film that won a specific award in 2022.
     * @param awardName The name of the award the movie won, inputted by the user
     */
    public void filmBudgetAndBoxOffice(String awardName) {
        // get the year's webpage and access the awards table
        Document yearDoc = getYearDocument("2022");
        Element table = yearDoc.select("table.wikitable").first();
        Element tbody = table.getElementsByTag("tbody").first();
        Elements rows = tbody.getElementsByTag("tr");

        // url section appended at the end of the general wikipedia link to get to the movie's page
        String addURL = "";

        // flag to determine if the award name was found or not
        boolean flag = false;

        // access each movie title in the award table
        for (Element row : rows) {
            Elements bulletBoxes = row.getElementsByTag("td");
            for (Element box : bulletBoxes) {
                Element b = box.getElementsByTag("div").first().getElementsByTag("b").first();
                String awardTitle = b.select("a").text();
                Element ul = box.getElementsByTag("ul").first();
                if (ul != null) {
                    for (Element li : ul.children()) {
                        Element ul2 = li.getElementsByTag("ul").first();
                        if (ul2 != null) {
                            for (Element li2 : ul.children()) {
                                Element movieTitle2 = li2.getElementsByTag("i").select("a").first();
                                // if award match, access the href link to go to the movie's webpage
                                if (awardTitle.equalsIgnoreCase(awardName)) {
                                    System.out.println("Movie title: " + movieTitle2.text());
                                    Elements i = li2.getElementsByTag("i");
                                    addURL = i.select("a").attr("href");
                                    // System.out.println(addURL);
                                    flag = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // if award not found, return invalid input
        if (!flag) {
            System.out.println("Invalid input");
        } else {
            try {
                // fetch the document by appending the url ending to the base url
                String movieLink = baseURL + addURL;
                System.out.println(movieLink);
                Document movieDoc = Jsoup.connect(URLDecoder.decode(movieLink)).get();

                // access logistical info box at top of movie page
                Element infoTable = movieDoc.select("table.infobox").first();
                Elements tableRows = infoTable.getElementsByTag("tbody").first().children();

                // track if the info was found or not
                boolean budgetFound = false;
                boolean boxOfficeFound = false;


                // go through the table and find the budget/box office sections if they exist
                for (Element row : tableRows) {
                    String heading = row.select("th").text().toLowerCase();
                    if (heading.equals("budget")) {
                        Element budg = row.getElementsByTag("td").first();
                        budgetFound = true;
                        if (budg != null) {
                            System.out.println("Budget: " + budg.text().split("\\[")[0]);
                        } else {
                            System.out.println("No budget found");
                        }
                    }

                    if (heading.equals("box office")) {
                        Element boxOff = row.getElementsByTag("td").first();
                        boxOfficeFound = true;
                        if (boxOff != null) {
                            System.out.println("Box Office: " + boxOff.text().split("\\[")[0]);
                        } else {
                            System.out.println("No box office found");
                        }
                    }
                }

                // return error messages if not found
                if (!budgetFound) {
                    System.out.println("No budget found");
                }

                if (!boxOfficeFound) {
                    System.out.println("No box office found");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Prints out the place of education that is the alma mater for the highest number of winners
     * of that specific award.
     * @param awardName The name of the award that we want the winners to have won.
     */
    public void mostAlumniNominated(String awardName) {
        // maps school to frequency of nominations
        schoolToCount = new HashMap<>();

        // 1933 doesn't have an academy awards page. starts 1927, ends 2022

        for (int i = 1927; i <= 2022; i++) {
            if (i != 1933) {
                System.out.println("\nYear: " + i);

                // retrieve page for that year's academy awards
                Document yearDoc = getYearDocument(String.valueOf(i));
                Element table = yearDoc.select("table.wikitable").first();
                Element tbody = table.getElementsByTag("tbody").first();
                Elements rows = tbody.getElementsByTag("tr");

                // the part of the URL to be appended for each nominee
                String addURL = "";

                // format 1: each box title has the bullets under it
                if (rows.first().getElementsByTag("td").first() != null) {
                    for (Element row : rows) {
                        // access each td element
                        Elements bulletBoxes = row.getElementsByTag("td");
                        for (Element box : bulletBoxes) {
                            Element div = box.getElementsByTag("div").first();
                            if (div == null) {
                                break;
                            }
                            Element b = div.getElementsByTag("b").first();

                            // find the award nap
                            String awardTitle = b.select("a").text();
                            Element ul = box.getElementsByTag("ul").first();
                            if (ul != null) {
                                for (Element li : ul.children()) {
                                    Element ul2 = li.getElementsByTag("ul").first();
                                    if (ul2 != null) {
                                        for (Element li2 : ul.children()) {
                                            // if there is a match with the award category we want
                                            if (awardTitle.equalsIgnoreCase(awardName)) {
                                                // winners in the bigger bullet points
                                                if (li2.getElementsByTag("b").first() != null) {
                                                    Element temp = li2.getElementsByTag("b").first();
                                                    addURL = temp.select("a").attr("href");
                                                    Element firstB = li2.getElementsByTag("b").first();
                                                    Element winner = firstB.select("a").first();
                                                    System.out.println("Winner: " + winner.text());
                                                    updateEducationTally(addURL);
                                                }

                                                // nominees under the smaller bullet points
                                                if (li2.getElementsByTag("ul").first() != null) {
                                                    Element nomineesUL = li2.getElementsByTag("ul").first();
                                                    Elements nomineesLIs = nomineesUL.getElementsByTag("li");
                                                    for (Element nomineeLI : nomineesLIs) {
                                                        addURL = nomineeLI.select("a").attr("href");
                                                        Element nominee = nomineeLI.select("a").first();
                                                        System.out.println("Nominee: " + nominee.text());
                                                        updateEducationTally(addURL);
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else { // format 2: each row lists 2 award categories, next row has 2 boxes of bullets
                    for (Element row : rows) {
                        Elements headers = row.getElementsByTag("th");
                        for (Element header : headers) {
                            String awardTitle = header.select("a").text();
                            // System.out.println(awardTitle);
                            if (awardTitle.equalsIgnoreCase(awardName)) {
                                // film winner
                                Element bullet = header.parent().nextElementSibling();

                                // 0 is left column, 1 is right column
                                int childNumber = 1;
                                if (header.equals(headers.first())) {
                                    childNumber = 0;
                                }

                                Element firstTD = bullet.getElementsByTag("td").get(childNumber);
                                Element ul = firstTD.getElementsByTag("ul").first();

                                // access the list of nominees
                                if (ul != null) {
                                    for (Element li : ul.children()) {
                                        Element ul2 = li.getElementsByTag("ul").first();
                                        if (ul2 != null) {
                                            for (Element li2 : ul.children()) {
                                                // if matches desired award
                                                if (awardTitle.equalsIgnoreCase(awardName)) {
                                                    // winners
                                                    if (li2.getElementsByTag("b").first() != null) {
                                                        Element temp = li2.getElementsByTag("b").first();
                                                        addURL = temp.select("a").attr("href");
                                                        Element firstB = li2.getElementsByTag("b").first();
                                                        Element winner = firstB.select("a").first();
                                                        System.out.println("Winner: " + winner.text());
                                                        updateEducationTally(addURL);
                                                    }

                                                    // nominees listed underneath
                                                    if (li2.getElementsByTag("ul").first() != null) {
                                                        Element nomUL = li2.getElementsByTag("ul").first();
                                                        Elements nomLIs = nomUL.getElementsByTag("li");
                                                        for (Element nomLI : nomLIs) {
                                                            addURL = nomLI.select("a").attr("href");
                                                            Element nominee = nomLI.select("a").first();
                                                            System.out.println("Nominee: " + nominee.text());
                                                            updateEducationTally(addURL);
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        // iterate through the hashmap to find the highest frequency alma mater
        Integer maxNominations = 0;
        String maxSchool = "";
        for (String school : schoolToCount.keySet()) {
            if (schoolToCount.get(school).compareTo(maxNominations) > 0) {
                maxSchool = school;
                maxNominations = schoolToCount.get(school);
            }
        }

        // print answer
        System.out.println("\n\nSchool: " + maxSchool + " with " + maxNominations + " nominations");
    }

    /**
     * Helper method to increment nomination tally for each education/alma mater school. For question 6.
     * @param addURL The added part of the URL for the given nominee.
     */
    public void updateEducationTally(String addURL) {
        try {
            // fetch the document by appending the url ending to the base url
            String winnerLink = baseURL + addURL;
            // System.out.println(winnerLink);
            Document winnerDoc = Jsoup.connect(URLDecoder.decode(winnerLink)).get();

            // access logistical info box at top of winner page
            Element infoTable = winnerDoc.select("table.infobox").first();
            if (infoTable != null) {
                Elements tableRows = infoTable.getElementsByTag("tbody").first().children();

                // go through the table and find the education/alma mater section if it exists
                for (Element row : tableRows) {
                    String heading = row.select("th").text().toLowerCase();
                    if (heading.equals("education")) {
                        Element education = row.getElementsByTag("td").first();
                        if (education != null) {
                            // get rid of the (BA) or (BS) etc.
                            String educ = education.text().split("\\(")[0].strip();
                            System.out.println("Education: " + educ);
                            schoolToCount.merge(educ, 1, Integer::sum);
                        }
                    }

                    if (heading.equals("alma mater")) {
                        Element almaMater = row.getElementsByTag("td").first();
                        if (almaMater != null) {
                            String alma = almaMater.text().split("\\(")[0].strip();
                            System.out.println("Alma Mater: " + alma);
                            schoolToCount.merge(alma, 1, Integer::sum);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds the countries that won/were nominated for the input award name in 2022.
     * Then goes to all previous years and tallies/prints the total nominations each of those
     * countries won for that award in previous years, including 2022.
     * @param awardName The user's input award name. Anything for which a movie can win is valid.
     */
    public void countryNominations(String awardName) {
        // maps school to frequency of nominations
        countryToCount = new HashMap<>();

        // Access each past year and if any of the countries we've seen already have been nominated
        // 1933 doesn't have an academy awards page. starts 1927, ends 2022, but we already counted 2022

        for (int i = 1927; i <= 2022; i++) {
            if (i != 1933) {
                System.out.println("\nYear: " + i);

                // retrieve page for that year's academy awards
                Document yearDoc2 = getYearDocument(String.valueOf(i));
                Element table2 = yearDoc2.select("table.wikitable").first();
                Element tbody2 = table2.getElementsByTag("tbody").first();
                Elements rows2 = tbody2.getElementsByTag("tr");

                // the part of the URL to be appended for each nominee
                String addURL2 = "";

                // format 1: each box title has the bullets under it
                if (rows2.first().getElementsByTag("td").first() != null) {
                    for (Element row : rows2) {
                        Elements bulletBoxes = row.getElementsByTag("td");
                        for (Element box : bulletBoxes) {
                            Element divEl = box.getElementsByTag("div").first();
                            if (divEl != null) {
                                Element b = divEl.getElementsByTag("b").first();
                                String awardTitle = b.select("a").text();
                                Element ul = box.getElementsByTag("ul").first();
                                if (ul != null) {
                                    for (Element li : ul.children()) {
                                        Element ul2 = li.getElementsByTag("ul").first();
                                        if (ul2 != null) {
                                            for (Element li2 : ul.children()) {
                                                Elements titleA = li2.getElementsByTag("i").select("a");
                                                Element title = titleA.first();
                                                // if award matched, access the href link to go to the movie's webpage
                                                if (awardTitle.equalsIgnoreCase(awardName)) {
                                                    System.out.println("Winner Movie: " + title.text());
                                                    Elements itag = li2.getElementsByTag("i");
                                                    addURL2 = itag.select("a").attr("href");
                                                    // System.out.println(addURL2);
                                                    updateCountryTally(addURL2);

                                                    // nominees under the smaller bullet points
                                                    if (li2.getElementsByTag("ul").first() != null) {
                                                        Element nomUL1 = li2.getElementsByTag("ul").first();
                                                        Elements nomineesLIs = nomUL1.getElementsByTag("li");
                                                        for (Element nomineeLI : nomineesLIs) {
                                                            Elements nomIs = nomineeLI.getElementsByTag("i");
                                                            Elements nomI1st = nomIs.first().select("a");
                                                            addURL2 = nomI1st.attr("href");
                                                            Element nominee = nomI1st.first();
                                                            if (nominee != null) {
                                                                System.out.println("Nominee Movie: " + nominee.text());
                                                                updateCountryTally(addURL2);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else { // format 2: each row lists 2 award categories, next row has 2 boxes of bullets
                    for (Element row : rows2) {
                        Elements headers = row.getElementsByTag("th");
                        for (Element header : headers) {
                            String awardTitle = header.select("a").text();
                            // System.out.println(awardTitle);
                            if (awardTitle.equalsIgnoreCase(awardName)) {
                                // film winner
                                Element bullet = header.parent().nextElementSibling();

                                // 0 is left column, 1 is right column
                                int childNumber = 1;
                                if (header.equals(headers.first())) {
                                    childNumber = 0;
                                }

                                Element firstTD = bullet.getElementsByTag("td").get(childNumber);
                                Element ul = firstTD.getElementsByTag("ul").first();

                                // access the list of nominees
                                if (ul != null) {
                                    for (Element li : ul.children()) {
                                        Element ul2 = li.getElementsByTag("ul").first();
                                        if (ul2 != null) {
                                            for (Element li2 : ul.children()) {
                                                Elements moviesA = li2.getElementsByTag("i").select("a");
                                                Element movieTitleSub = moviesA.first();
                                                // if matches desired award
                                                if (awardTitle.equalsIgnoreCase(awardName)) {
                                                    System.out.println("Winner Movie: " + movieTitleSub.text());
                                                    Elements itag = li2.getElementsByTag("i");
                                                    addURL2 = itag.select("a").attr("href");
                                                    System.out.println(addURL2);
                                                    updateCountryTally(addURL2);

                                                    // nominees under the smaller bullet points
                                                    if (li2.getElementsByTag("ul").first() != null) {
                                                        Element nomULs = li2.getElementsByTag("ul").first();
                                                        Elements nomLIs = nomULs.getElementsByTag("li");
                                                        for (Element nLI : nomLIs) {
                                                            Element nomI = nLI.getElementsByTag("i").first();
                                                            addURL2 = nomI.select("a").attr("href");
                                                            Element nominee = nomI.select("a").first();
                                                            System.out.println("Nominee Movie: " + nominee.text());
                                                            updateCountryTally(addURL2);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        System.out.println("The following is each country winner/nominee for the " + awardName + " award and" +
                "the number of nominations they have received in the past, including this year.");

        // prints mapping of values
        for (String countryKey : countryToCount.keySet()) {
            System.out.println("Country: " + countryKey + " Nominations: " + countryToCount.get(countryKey));
        }
    }

    /**
     * Helper method for Q7 to count up the winners/nominees of the award category.
     * Accounts for various formats of the country section for each movie webpage.
     * @param addURL The add-on part of the URL for that specific movie to check for the country.
     */
    public void updateCountryTally (String addURL) {
        try {
            // fetch the document by appending the url ending to the base url
            String movieLink = baseURL + addURL;
            System.out.println(movieLink);
            Document movieDoc = Jsoup.connect(URLDecoder.decode(movieLink)).get();

            // access logistical info box at top of movie page
            Element infoTable = movieDoc.select("table.infobox").first();
            if (infoTable != null) {
                Elements tableRows = infoTable.getElementsByTag("tbody").first().children();

                // go through the table and find the country section if it exists
                for (Element row : tableRows) {
                    String heading = row.select("th").text().toLowerCase();
                    // for singular countries listed
                    if (heading.equals("country")) {
                        Element country = row.getElementsByTag("td").first();
                        if (country != null) {
                            String countryName = country.text().split("\\[")[0].strip();
                            System.out.println("Country: " + countryName);
                            countryToCount.merge(countryName, 1, Integer::sum);
                        }
                    }

                    // two possible formats if multiple countries listed
                    if (heading.equals("countries")) {
                        Element country = row.getElementsByTag("td").first();
                        if (country != null) {
                            Element div = country.getElementsByTag("div").first();
                            if (div != null) {
                                Element ul = country.getElementsByTag("ul").first();
                                // first format is listed under lis
                                if (ul != null) {
                                    Elements lis = ul.getElementsByTag("li");
                                    if (lis.first() != null) {
                                        for (Element li : lis) {
                                            // get rid of in-text citations
                                            String subCountry = li.text().split("\\[")[0].strip();
                                            System.out.println("Countries: " + subCountry);
                                            countryToCount.merge(subCountry, 1, Integer::sum);
                                        }
                                    }
                                }
                            } else {
                                // second format is split by <br> tags
                                String[] text = country.outerHtml().split("<br>");
                                for (String s : text) {
                                    String line = s.strip();
                                    if (!line.contains("<")) {
                                        String country1 = line.split("\\[")[0];
                                        System.out.println("Countries br: " + country1);
                                        countryToCount.merge(country1, 1, Integer::sum);
                                    } else if (line.indexOf("<") == 0) {
                                        String country2 = line.substring(line.indexOf(">") + 1);
                                        country2 = country2.split("\\[")[0];
                                        System.out.println("Countries br: " + country2);
                                        countryToCount.merge(country2, 1, Integer::sum);
                                    } else {
                                        String country3 = line.substring(0, line.indexOf("<"));
                                        country3 = country3.split("\\[")[0];
                                        System.out.println("Countries br: " + country3);
                                        countryToCount.merge(country3, 1, Integer::sum);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the average age of all winners/nominees for a given award category across the years,
     * assuming everyone was still alive now.
     * @param awardName The user's desired award category.
     */
    public void averageAge(String awardName) {
        // initializes values to find the average
        ageCount = 0;
        birthYearSum = 0;

        // 1933 doesn't have an academy awards page. starts 1927, ends 2022

        for (int i = 1927; i <= 2022; i++) {
            if (i != 1933) {
                System.out.println("\nYear: " + i);

                // retrieve page for that year's academy awards
                Document yearDoc = getYearDocument(String.valueOf(i));
                Element table = yearDoc.select("table.wikitable").first();
                Element tbody = table.getElementsByTag("tbody").first();
                Elements rows = tbody.getElementsByTag("tr");

                // the part of the URL to be appended for each nominee
                String addURL = "";

                // format 1: each box title has the bullets under it
                if (rows.first().getElementsByTag("td").first() != null) {
                    for (Element row : rows) {
                        // access each td element
                        Elements bulletBoxes = row.getElementsByTag("td");
                        for (Element box : bulletBoxes) {
                            Element div = box.getElementsByTag("div").first();
                            if (div == null) {
                                break;
                            }
                            Element b = div.getElementsByTag("b").first();

                            // find the award nap
                            String awardTitle = b.select("a").text();
                            Element ul = box.getElementsByTag("ul").first();
                            if (ul != null) {
                                for (Element li : ul.children()) {
                                    Element ul2 = li.getElementsByTag("ul").first();
                                    if (ul2 != null) {
                                        for (Element li2 : ul.children()) {
                                            // if there is a match with the award category we want
                                            if (awardTitle.equalsIgnoreCase(awardName)) {
                                                // winners in the bigger bullet points
                                                if (li2.getElementsByTag("b").first() != null) {
                                                    Element temp = li2.getElementsByTag("b").first();
                                                    addURL = temp.select("a").attr("href");
                                                    Element firstB = li2.getElementsByTag("b").first();
                                                    Element winner = firstB.select("a").first();
                                                    System.out.println("Winner: " + winner.text());
                                                    updateAgeAverage(addURL);
                                                }

                                                // nominees under the smaller bullet points
                                                if (li2.getElementsByTag("ul").first() != null) {
                                                    Element nomineesUL = li2.getElementsByTag("ul").first();
                                                    Elements nomineesLIs = nomineesUL.getElementsByTag("li");
                                                    for (Element nomineeLI : nomineesLIs) {
                                                        addURL = nomineeLI.select("a").attr("href");
                                                        Element nominee = nomineeLI.select("a").first();
                                                        System.out.println("Nominee: " + nominee.text());
                                                        updateAgeAverage(addURL);
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else { // format 2: each row lists 2 award categories, next row has 2 boxes of bullets
                    for (Element row : rows) {
                        Elements headers = row.getElementsByTag("th");
                        for (Element header : headers) {
                            String awardTitle = header.select("a").text();
                            // System.out.println(awardTitle);
                            if (awardTitle.equalsIgnoreCase(awardName)) {
                                // film winner
                                Element bullet = header.parent().nextElementSibling();

                                // 0 is left column, 1 is right column
                                int childNumber = 1;
                                if (header.equals(headers.first())) {
                                    childNumber = 0;
                                }

                                Element firstTD = bullet.getElementsByTag("td").get(childNumber);
                                Element ul = firstTD.getElementsByTag("ul").first();

                                // access the list of nominees
                                if (ul != null) {
                                    for (Element li : ul.children()) {
                                        Element ul2 = li.getElementsByTag("ul").first();
                                        if (ul2 != null) {
                                            for (Element li2 : ul.children()) {
                                                // if matches desired award
                                                if (awardTitle.equalsIgnoreCase(awardName)) {
                                                    // winners
                                                    if (li2.getElementsByTag("b").first() != null) {
                                                        Element temp = li2.getElementsByTag("b").first();
                                                        addURL = temp.select("a").attr("href");
                                                        Element firstB = li2.getElementsByTag("b").first();
                                                        Element winner = firstB.select("a").first();
                                                        System.out.println("Winner: " + winner.text());
                                                        updateAgeAverage(addURL);
                                                    }

                                                    // nominees listed underneath
                                                    if (li2.getElementsByTag("ul").first() != null) {
                                                        Element nomUL = li2.getElementsByTag("ul").first();
                                                        Elements nomLIs = nomUL.getElementsByTag("li");
                                                        for (Element nomLI : nomLIs) {
                                                            addURL = nomLI.select("a").attr("href");
                                                            Element nominee = nomLI.select("a").first();
                                                            System.out.println("Nominee: " + nominee.text());
                                                            updateAgeAverage(addURL);
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        double average = 0;

        // calculates the average if more than 0 data points
        if (ageCount != 0) {
            double averageBirthYear = (double) birthYearSum / ageCount;
            average = 2022 - averageBirthYear;
        }
        // print answer
        System.out.println("\n\nAverage Age (if everyone was still alive now): " + average);
    }

    /**
     * Updates the sum and count variables for calculating the average.
     * @param addURL Input add-on part of the URL to access the nominee's webpage
     */
    public void updateAgeAverage(String addURL) {
        try {
            // fetch the document by appending the url ending to the base url
            String winnerLink = baseURL + addURL;
            // System.out.println(winnerLink);
            Document winnerDoc = Jsoup.connect(URLDecoder.decode(winnerLink)).get();

            // access logistical info box at top of winner page
            Element infoTable = winnerDoc.select("table.infobox").first();
            if (infoTable != null) {
                Elements tableRows = infoTable.getElementsByTag("tbody").first().children();

                // go through the table and find the born if it exists
                for (Element row : tableRows) {
                    String heading = row.select("th").text().toLowerCase();
                    if (heading.equals("born")) {
                        Element birthday = row.getElementsByTag("td").first();
                        if (birthday != null) {
                            String bday = birthday.text();
                            String birthYear = bday.substring(bday.indexOf("(") + 1, bday.indexOf("(") + 5);
                            System.out.println("Birth year: " + birthYear);
                            try {
                                // finds the birth year add increments the sum/count accordingly
                                Integer nomineeYear = Integer.parseInt(birthYear);
                                birthYearSum += nomineeYear;
                                ageCount++;
                            } catch (NumberFormatException e) {
                                System.out.println("Not a year");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}