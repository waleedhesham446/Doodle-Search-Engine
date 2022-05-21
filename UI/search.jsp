<!DOCTYPE html>
<html lang="en" dir="ltr">

<head>
    <meta charset="utf-8">
    <title>Autocomplete Search Box</title>
    <link rel="stylesheet" href="./style.css">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css" />
    <script src="https://kit.fontawesome.com/afc97c838a.js" crossorigin="anonymous"></script>
    <style>
        h1 {
            text-align: center;
            position: absolute;
            top: 12%;
            left: 50%;
            transform: translate(-50%, -50%);
            color: #ffff;
            font-weight: bolder;
            font-size: 89px;
            letter-spacing: -5px;
        }
    </style>
</head>

<body>
    <h1>Search</h1>
    <div class="wrapper">
        <div class="search-input">
            <a href="" target="_blank" hidden></a>
            <input type="text" placeholder="Type to search..">
            <div class="autocom-box">
                <!-- here list are inserted from javascript -->
            </div>
            <div class="icon" onclick="runSpeechRecognition()"><i class="fa-solid fa-microphone"></i></div>
        </div>
        <div id="output" class="hide"></div>
        <div id="output" class="hide"></div>
    </div>

    <script src="./main.js"></script>
    <script src="./s.js"></script>

    <form style="display: none;" action="NameGenderRequest" method="GET" id="AppRequest">
       <input type="text" name="WORD_Query" id="WORD_Query"/> 
        <br>
        <br>
        <input type="submit" value="Submit" />
    </form>
    <%@ page import="java.util.*" %>
    <% ArrayList<String> suggestionsArr = (ArrayList<String>) request.getAttribute("suggestions"); %>
</body>
<script>
    var suggestions =<%= suggestionsArr%>;
    console.log(suggestions);
    
</script>
</html>