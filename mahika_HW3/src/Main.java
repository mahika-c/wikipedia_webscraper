import java.util.Scanner;

/**
 * Class for user to interact with. Displays numbered trivia style questions,
 * and user picks a question as well as fills in the italicized sections.
 * Uses the Scanner class to retrieve input from the user.
 * Uses methods from the WebScraper class to display the result.
 *
 * Author: Mahika Calyanakoti
 */
public class Main {
    public static void main(String[] args) {
        // backend to parse HTML
        WebScraper ws = new WebScraper("https://en.wikipedia.org/wiki/Academy_Awards");

        // to take in user input
        Scanner sc = new Scanner(System.in);

        // display questions
        System.out.println("Please input question number you want:\n" +
                "1. List all discontinued categories.\n" +
                "2. How many award categories were added in the [DECADE]?\n" +
                "3. List all movies nominated for at least [MINIMUM] awards in [YEAR].\n" +
                "4. Which film won [AWARD NAME] in [YEAR]?\n" +
                "5. What was the budget for the [AWARD NAME] winner in 2022? " +
                "How much did this movie make in the box office?\n" +
                "6. Which academic institution (university, college, etc.) has the highest number" +
                "of alumni nominated for the [AWARD NAME] award?\n" +
                "7. For [AWARD NAME], for the countries that were nominated/won, how many times " +
                "have they been nominated in the past (including this year)?\n" +
                "8. Wild card - What is the average age of the winners for [AWARD NAME] across all years?");

        // take in question number and ask for input accordingly.
        // then call the corresponding webscraper method to print out the answer;
        String questionNum = sc.nextLine();
        switch (questionNum) {
            case "1" -> ws.discontinuedCategories();
            case "2" -> {
                System.out.println("What is the first year of the decade? (End in 0)\n");
                String decade = sc.nextLine();
                ws.addedCategories(decade);
            }
            case "3" -> {
                System.out.println("What is the minimum number of awards? Please type a number\n");
                String minimum = sc.nextLine();
                System.out.println("What is the year?\n");
                String year = sc.nextLine();
                ws.miminumNominations(minimum, year);
            }
            case "4" -> {
                System.out.println("What is the award name?\n");
                String awardName = sc.nextLine();
                System.out.println("What is the year?\n");
                String year = sc.nextLine();
                ws.winningFilm(awardName, year);
            }
            case "5" -> {
                System.out.println("What is the award name?\n");
                String awardName = sc.nextLine();
                ws.filmBudgetAndBoxOffice(awardName);
            }
            case "6" -> {
                System.out.println("What is the award name?\n");
                String awardName = sc.nextLine();
                ws.mostAlumniNominated(awardName);
            }
            case "7" -> {
                System.out.println("What is the award name?\n");
                String awardName = sc.nextLine();
                ws.countryNominations(awardName);
            }
            case "8" -> {
                System.out.println("What is the award name?\n");
                String awardName = sc.nextLine();
                ws.averageAge(awardName);
            }
            default -> System.out.println("Invalid input. Please run MAIN again!\n");
        }
    }
}