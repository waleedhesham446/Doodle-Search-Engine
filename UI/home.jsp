<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css" />
    <script src="https://kit.fontawesome.com/afc97c838a.js" crossorigin="anonymous"></script>
    <title>results</title>
    <link rel="stylesheet" href="style2.css">
    <style>
        .hide {
            display: none;
        }
        
        .link_container {
            margin: 10px;
            padding: 5px;
        }
        
        .link_container .url_container {
            padding: 10px;
            font-size: bold;
            font-weight: 20px;
        }

        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@200;300;400;500;600;700&display=swap');
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Poppins', sans-serif;
        }

        body {
            background: #E9FAE3;
            padding: 0 20px;
        }


        /* ::selection {
            color: #fff;
            background: #664AFF;
        } */

        .wrapper {
            max-width: 450px;
            margin: 10px 0;
            text-align: left;
        }

        .wrapper .search-input {
            background: #fff;
            width: 100%;
            border-radius: 5px;
            position: relative;
            box-shadow: 0px 1px 5px 3px rgba(0, 0, 0, 0.12);
            display: flex;
            justify-content: right;
        }

        .search-input input {
            height: 55px;
            width: 100%;
            outline: none;
            border: none;
            border-radius: 5px;
            padding: 0 60px 0 20px;
            font-size: 18px;
            box-shadow: 0px 1px 5px rgba(0, 0, 0, 0.1);
        }

        .search-input.active input {
            border-radius: 5px 5px 0 0;
            -webkit-border-radius: 5px 5px 0 0;
            -moz-border-radius: 5px 5px 0 0;
            -ms-border-radius: 5px 5px 0 0;
            -o-border-radius: 5px 5px 0 0;
        }

        .search-input .autocom-box {
            padding: 0;
            opacity: 0;
            pointer-events: none;
            max-height: 280px;
            overflow-y: auto;
        }

        .search-input.active .autocom-box {
            padding: 10px 8px;
            opacity: 1;
            pointer-events: auto;
        }

        .autocom-box li {
            list-style: none;
            padding: 8px 12px;
            display: none;
            width: 100%;
            cursor: default;
            border-radius: 3px;
        }

        .search-input.active .autocom-box li {
            display: block;
        }

        .autocom-box li:hover {
            background: #efefef;
        }

        .search-input .icon {
            position: absolute;
            right: 0px;
            top: 0px;
            height: 55px;
            width: 55px;
            text-align: center;
            line-height: 55px;
            font-size: 20px;
            color: #644bff;
            cursor: pointer;
        }

        .link_container {
            width: 950px;
            /* background-color: #DEE8D5; */
            font-size: 23px;
        }

        .url {
            text-decoration: none;
        }

        .url:hover {
            text-decoration: underline;
        }

        .description {
            font-size: 15px;
            color: gray;
        }
    </style>
</head>

<body>
<%@ page import="java.util.*" %>
    <% ArrayList<String> sortedLinks1 = (ArrayList<String>) request.getAttribute("links"); %>
    <% ArrayList<String> sortedTitles1 = (ArrayList<String>) request.getAttribute("titles"); %>
    <% ArrayList<String> sortedDescriptions1 = (ArrayList<String>) request.getAttribute("descriptions"); %>


    <div class="icon" onclick="runSpeechRecognition()"><i class="fa-solid fa-microphone"></i></div>
        </div>
        <div id="output" class="hide"></div>
        <div id="output" class="hide"></div>
    </div>
    <div id="link" class="link_container hide">
        <div class="url_container">
            <a class="url">

            </a>
        </div>
        <div class="description_container">
            <p class="description">

            </p>
        </div>
    </div>
    <div id="resultsContainer"></div>
    <div class="pageination_container">
        <button id="nextPage">F</button>
        <button id="prevPage">B</button>
    </div>
</body>
<script>
    var sortedLinks =<%= sortedLinks1%>;
    var sortedTitles =<%= sortedTitles1%>;
    var sortedDescriptions =<%= sortedDescriptions1%>;

    const resultsContainer = document.getElementById('resultsContainer');

    let currentPage = 0;
    let numLinks = sortedLinks.length;
    let numPages = numLinks / 10;

    let limit = 10 > numLinks ? numLinks : 10;
    for (let i = 0; i < limit; i++) {
        let newlink = document.getElementById("link").cloneNode(true);
        newlink.setAttribute("id", `link-${i}`);
        
        newlink.classList.remove("hide");
        newlink.children[0].children[0].textContent = sortedTitles[i];
        newlink.children[0].children[0].setAttribute("href", sortedLinks[i]);
        newlink.children[1].children[0].textContent = sortedDescriptions[i];
        resultsContainer.appendChild(newlink);
    }

    document.getElementById('nextPage').onclick = () => {
        if(currentPage+1 == numPages) return;
        
        currentPage++;
        resultsContainer.innerHTML = '';
        let limit = ((currentPage+1)*10) >= numLinks ? numLinks : (currentPage+1)*10;
        for (let i = currentPage*10; i < limit; i++) {
            let newlink = document.getElementById("link").cloneNode(true);
            newlink.setAttribute("id", `link-${i}`);
            
            newlink.classList.remove("hide");
            newlink.children[0].children[0].textContent = sortedTitles[i];
            newlink.children[0].children[0].setAttribute("href", sortedLinks[i]);
            newlink.children[1].children[0].textContent = sortedDescriptions[i];
            resultsContainer.appendChild(newlink);
        }
    }

    document.getElementById('prevPage').onclick = () => {
        if(currentPage-1 == -1) return;
        
        currentPage--;
        resultsContainer.innerHTML = '';
        let limit = ((currentPage+1)*10) >= numLinks ? numLinks : (currentPage+1)*10;
        for (let i = currentPage*10; i < limit; i++) {
            let newlink = document.getElementById("link").cloneNode(true);
            newlink.setAttribute("id", `link-${i}`);
            
            newlink.classList.remove("hide");
            newlink.children[0].children[0].textContent = sortedTitles[i];
            newlink.children[0].children[0].setAttribute("href", sortedLinks[i]);
            newlink.children[1].children[0].textContent = sortedDescriptions[i];
            resultsContainer.appendChild(newlink);
        }
    }
</script>
</html>