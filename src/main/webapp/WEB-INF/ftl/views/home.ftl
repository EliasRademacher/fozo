<!doctype html>
<html lang="en">
    <head>
        <#-- this comes from HelloWorld.java, when we added
              model.addAttribute("pageTitle", "Example Freemarker Page"); -->
        <title>${pageTitle}</title>
    </head>
    <body>

        <a href="/viewAllPeople">View All People</a>

        <h2>Welcome!</h2>

        <p>${personAddedMessage}</p>

        <form name="person" action="home" method="post">
          Pick a username:<input type="text" name="userName"><br>

           <input type="radio" name="ethnicity" value="native"checked> Native American<br>
           <input type="radio" name="ethnicity" value="white"> Of European Descent<br>
           <input type="radio" name="ethnicity" value="black"> African American<br>
           <input type="radio" name="ethnicity" value="latino"> Latino<br>
           <input type="radio" name="ethnicity" value="other"> Other<br>

           Date of Birth: <input type="date" name="birthDate"><br>

           Email Address: <input type="email" name="email"><br>

           I am a U.S. citizen <input type="checkbox" name="usCitizen" value="true"><br>

           <input type="submit" value="Submit"><br>
        </form>


    </body>
</html>
