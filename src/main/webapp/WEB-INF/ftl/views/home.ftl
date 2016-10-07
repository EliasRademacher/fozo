<!doctype html>
<html lang="en">
    <head>
        <#-- this comes from HelloWorld.java, when we added
              model.addAttribute("pageTitle", "Example Freemarker Page"); -->
        <title>${pageTitle}</title>
    </head>
    <body>
        <h2>Welcome!</h2>

        <form name="person" action="home.html" method="post">
          Pick a username:<input type="text" name="userName">
          <input type="submit" value="Submit">
        </form>


    </body>
</html>
