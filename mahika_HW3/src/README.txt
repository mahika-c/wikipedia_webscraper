Mahika Calyanakoti
PennID: 42090313
NETS 1500 HW3 - Programming
Wikipedia and the Oscars - https://en.wikipedia.org/wiki/Academy_Awards
4/4/2023

-----------------------------------------------------------------------
Description of Files:

Main.java - For the user to interact with. Displays questions so user
can choose and fill in the blanks using the Scanner class, then returns
the answer on the screen. Answers one question and can be rerun to answer
more questions.

WebScraper.java - The main backend of the project. Uses the JSoup library
to parse through the HTML of the Wikipedia page to find answers to the
user's questions.

I've included the URLGetter and URLTester methods, which show my
getRedirectedURL() method, but are not used for the programming part.

The pdf in this folder has my theory write

-----------------------------------------------------------------------
Decisions:

- I chose to use JSoup for ease of navigating the HTML, while using
regex to match the specific pattern based on the user's questions.
- For Main.java valid input: For the question number, it must be
between 1 and 8. For the italicized input, my code fixes capital letter
issues, but assumes there are no typos or input of things that don't exist
(ex. asking for an award category that doesn't exist)
- I created a helper method to remove redundant code that retrieves
the page for a specific input year.
- We assume that the year input refers to movies made in that year, where the ceremony
  is held in the next year. Ex. For 2022 movies, we look at the 95th Academy Awards, held in 2023.
- Winning an award implies that it was nominated. Ex. The 1st Academy Awards were for 1927 movies.
- Person-specific awards like Best Director are counted as nominations for the movie they are winning
  the award for.
- Checkstyle - Certain potential null pointers I had to overlook since I can assume those values won't
be null. After running many tests, they haven't caused any issues. Though, I did check for potential
null values where appropriate. Also some lines are over 100 characters simply because of the if/for loops
that can't really be avoided easily. Overall though, I kept the lines short, variable names clear, and checked
for excpetions where necessary.

-----------------------------------------------------------------------
Assumptions:

- Q1 - Accessed "Discontinued Categories" section. Doesn't include "special" categories.

- Q2 - Accessed table titled "List of current Awards of Merit categories by year
 introduced, sortable by category" "Current categories" section. Asks user to input
 the first year of a decade (ends in 0), but the code works even if the year doesn't
 end in 0, since we only look at the first 3 digits of the year for the decade.
 Doesn't include "special" categories. If the decade has no award categories created, then
 simply nothing is printed.

- Q3 - I want to allow for the user to ask for at least 1 nomination, so I can't use the "Multiple Nominations"
table, since it doesn't account for the minimum of 1 nomination case. That's why I had to iterate through
all the nominations in the table and tally them myself. Assumes the user inputs a number for minimum number of
nominations. If the threshold is too high, nothing is printed after "The movies nominated at least __ times are:"

- Q4 - To account for case differences in the user input, I used ignored case on the input and when
parsing the HTML to standardize the match. We assume there are no spelling typos in the user query. If
the award name can't be found, "Invalid input" is printed.
My code accounts for two types of table formats:
1: each box has a heading that is the award name and below it has a bulleted box
2: there is a row with two "th" sections, each corresponding to an award title. The row below it has 2 bullet boxes.

- Q5 - To account for case differences in the user input, I ignored case on the input and when
       parsing the HTML to standardize the match. We assume there are no spelling typos in the user query. If
       the award name can't be found, "Invalid input" is printed.

- Q6 - Only existent alma mater fields should count for actor/actress education. (See Ed post #339)
Also, as per Ed post #354, we can assume only current categories are used for input.
As per Ed post #270, we assume the categories are related to people specifically, like Best Director/Actor/etc.
If there is a tie, one of the correct answers is printed.
If the alma mater is not clearly listed under the Education, we skip them.
I wanted to clearly print each year's winner/nominee names as well as their education/alma mater printed underneath
so that while the user is waiting for the final calculation, they can see the data being fetched.

- Q7 - For inputs, I will accept any award category for which a movie can win, because you can find the country
associated with a movie on the movie's website.
When accessing the country fields, I had to deal with different formats, such a singular country and multiple
listed countries. When there were multiple countries, sometimes they were listed under <li> tags, and sometimes
they were listed separated by <br>, so I accounted for these cases in my code.

The method will print nothing under a year when an award category didn't exist in that year. For instance,
Best Picture or Best Interntl Film didn't exist in certain years. So the tally just simply will skip over the years.
The method prints the info as it fetches it.

- Q8 - I wanted my question to have a statistical meaning, so I wanted to find the average age of the winners
and nominees in a given category across all years of the Oscars. Assumes input that has to do with a person winning,
such as Best Actor, Actress, Director, etc.

To calculate the age, I had to take the average birth year by extracting that data point from each nominee's
wikipedia page, and then subtracting 2022. This assumes that everyone is still alive now, but I thought it was
a cool statistic to play around with.