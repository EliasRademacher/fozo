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
            <h3>${entity.getProperty("userName")}</h3>
            Birthdate: ${entity.getProperty("birthDate")?date}<br>
            Email Address: ${entity.getProperty("email")}<br>
            Ethnicity: ${entity.getProperty("ethnicity")}<br>
            Join Date: ${entity.getProperty("joinDate")?date}<br>
            U.S. Citizen? ${entity.getProperty("usCitizen")?then('Yes', 'No')}<br>
        </#list>

    </body>
</html>