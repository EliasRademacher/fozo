<!doctype html>
<html lang="en">
    <head>
        <title>${pageTitle}</title>
    </head>
    <body>
        <a href="/home">Add a new Person</a>
        <h2>All People</h2>

        <h4>Number of People Retrieved: ${numberOfPeople}</h4>
        <#list allPeopleEntities as entity>
        <h3>
            ${entity.getProperty("userName")}
            <a href="/edit?userName=${entity.getProperty("userName")}"> Edit</a>
        </h3>
            Birthdate: ${(entity.getProperty("birthDate")?date)!"unknown"}<br>
            Email Address: ${(entity.getProperty("email"))!"unknown"}<br>
            Ethnicity: ${(entity.getProperty("ethnicity"))!"unknown"}<br>
            Join Date: ${(entity.getProperty("joinDate")?date)!"unknown"}<br>
            U.S. Citizen? ${(entity.getProperty("usCitizen")?then('Yes', 'No'))!"unknown"}<br>
        </#list>

    </body>
</html>