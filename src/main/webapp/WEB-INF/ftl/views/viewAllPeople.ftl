<!doctype html>
<html lang="en">
    <head>
        <title>${pageTitle}</title>
    </head>
    <body>

        <a href="/home">Add a new Person</a>

        <h2>All People</h2>

        Number of People Retrieved: ${numberOfPeople}

        <h4>Here are all the people:</h4>
        <#list allPeopleEntities as entity>
            Person: ${entity.getProperty("userName")}</br>
        </#list>

    </body>
</html>