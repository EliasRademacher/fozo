<!doctype html>
<html lang="en">
<head>
    <title>${pageTitle}</title>
</head>
<body>
    <a href="/home">Add a new Person</a>
    <a href="/viewAllPeople">View All People</a>
    <h2>Edit ${personEntityToEdit.getProperty("userName")!"Person"}'s Information</h2>

    <h3>${personEditedMessage}</h3>

    <#assign email=(personEntityToEdit.getProperty("email"))!"unknown">
    <#assign birthDate=(personEntityToEdit.getProperty("birthDate")?date)!"unknown">
    <#assign ethnicity=(personEntityToEdit.getProperty("ethnicity"))!"unknown">
    <#assign usCitizen=(personEntityToEdit.getProperty("usCitizen")?then('true', 'false'))!"unknown">
    <#assign joinDate=(personEntityToEdit.getProperty("joinDate")?date)!"unknown">
    <#assign userName=(personEntityToEdit.getProperty("userName"))!"unknown">

    <#assign native="">
    <#assign white="">
    <#assign black="">
    <#assign latino="">
    <#assign other="">

    <#if ethnicity?contains("native")>
        <#assign native="checked">
    <#elseif ethnicity?contains("white")>
        <#assign white="checked">
    <#elseif ethnicity?contains("black")>
        <#assign black="checked">
    <#elseif ethnicity?contains("latino")>
        <#assign latino="checked">
    <#else>
        <#assign other="checked">
    </#if>

    <form name="editedPerson" action="edit" method="post">
        <input type="radio" name="ethnicity" value="native" ${native}> Native American<br>
        <input type="radio" name="ethnicity" value="white" ${white}> Of European Descent<br>
        <input type="radio" name="ethnicity" value="black" ${black}> African American<br>
        <input type="radio" name="ethnicity" value="latino" ${latino}> Latino<br>
        <input type="radio" name="ethnicity" value="other" ${other}> Other<br>

        Date of Birth: <input type="date" name="birthDate" value="${birthDate?string.short}"><br>
        Email Address: <input type="email" name="email" value="${email}"><br>
        U.S. citizen <input type="checkbox" name="usCitizen"><br>

        <input type="hidden" name="joinDate" value="${joinDate?string.short}">
        <input type="hidden" name="userName" value="${userName}">

        <input type="submit" value="Submit Changes"><br>
    </form>

</body>
</html>