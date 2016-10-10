<!doctype html>
<html lang="en">
    <head>
        <title>${pageTitle!"Add a Person"}</title>
    </head>
    <body>
        <a href="/viewAllPeople">View All People</a>
        <h2>Welcome!</h2>
        <p>${personAddedMessage!"\n"}</p>

        <form name="person" action="home" method="post">
          Pick a username:<input type="text" name="userName"><br>

           <input type="radio" name="ethnicity" value="native"checked> Native American<br>
           <input type="radio" name="ethnicity" value="white"> Of European Descent<br>
           <input type="radio" name="ethnicity" value="black"> African American<br>
           <input type="radio" name="ethnicity" value="latino"> Latino<br>
           <input type="radio" name="ethnicity" value="other"> Other<br>

           Date of Birth: <input type="date" name="birthDate" placeholder="MM/dd/yyyy"><br>
           Email Address: <input type="email" name="email"><br>
           I am a U.S. citizen <input type="checkbox" name="usCitizen" checked><br>

           <input type="submit" value="Submit"><br>
        </form>


    </body>
</html>
