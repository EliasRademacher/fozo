<!doctype html>
<html lang="en">
<head>
    <title>${pageTitle}</title>
</head>
<body>
    <a href="/home">Add a new Person</a>
    <a href="/viewAllPeople">View All People</a>
    <h2>Edit ${personEntityToEdit.getProperty("userName")}'s Information</h2>

    Birthdate: ${(personEntityToEdit.getProperty("birthDate")?date)!"unknown"}<br>
    Email Address: ${(personEntityToEdit.getProperty("email"))!"unknown"}<br>
    Ethnicity: ${(personEntityToEdit.getProperty("ethnicity"))!"unknown"}<br>
    Join Date: ${(personEntityToEdit.getProperty("joinDate")?date)!"unknown"}<br>
    U.S. Citizen? ${(personEntityToEdit.getProperty("usCitizen")?then('Yes', 'No'))!"unknown"}<br>


</body>
</html>