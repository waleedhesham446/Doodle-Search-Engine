const searchWrapper = document.querySelector(".search-input");
const inputBox = searchWrapper.querySelector("input");
const suggBox = searchWrapper.querySelector(".autocom-box");
const icon = searchWrapper.querySelector(".icon");
let linkTag = searchWrapper.querySelector("a");
let webLink;

inputBox.onkeyup = (e) => {
    let userData = e.target.value;
    let emptyArray = [];
    if (userData) {
        if (e.key === "Enter") {
            console.log(1);
        document.getElementById("WORD_Query").value=userData;
        document.getElementById("AppRequest").submit();
        
        }
        // emptyArray = suggestions.filter((data) => {
        //     return data.toLocaleLowerCase().startsWith(userData.toLocaleLowerCase());
        // });
        // emptyArray = emptyArray.map((data) => {
        //     return data = `<li>${data}</li>`;
        // });
        // searchWrapper.classList.add("active");
        // showSuggestions(emptyArray);
        // let allList = suggBox.querySelectorAll("li");
        // allList.forEach(e => {
        //     e.setAttribute("onclick", "select(this)");
        // });
    } else {
        searchWrapper.classList.remove("active");
    }
}

function select(element) {
    let selectData = element.textContent;
    inputBox.value = selectData;

    // webLink = `https://www.google.com/search?q=${selectData}`;
    // linkTag.setAttribute("href", webLink);
    // linkTag.click();

    searchWrapper.classList.remove("active");
}

function showSuggestions(list) {
    let listData;
    if (!list.length) {
        userValue = inputBox.value;
        listData = `<li>${userValue}</li>`;
    } else {
        listData = list.join('');
    }
    suggBox.innerHTML = listData;
    // console.log(listData)
}

function runSpeechRecognition() {
    // get output div reference
    var output = document.getElementById("output");
    // get action element reference
    var action = document.getElementById("action");
    // new speech recognition object
    var SpeechRecognition = SpeechRecognition || webkitSpeechRecognition;
    var recognition = new SpeechRecognition();


    // This runs when the speech recognition service starts
    recognition.onstart = function() {
        action.innerHTML = "<small>I'm listening, please speak...</small>";
    };

    recognition.onspeechend = function() {
        action.innerHTML = "<small>stopped listening...</small>";
        recognition.stop();
    }

    // This runs when the speech recognition service returns result
    recognition.onresult = function(event) {
        var transcript = event.results[0][0].transcript;
        var confidence = event.results[0][0].confidence;

        document.getElementById("WORD_Query").value=transcript;
        // setTimeout(() => {
            document.getElementById("AppRequest").submit();
        // }, 2000);
        // output.innerHTML = "<b>Text:</b> " + transcript + "<br/> <b>Confidence:</b> " + confidence * 100 + "%";
        // output.classList.remove("hide");
        // webLink = `https://www.google.com/search?q=${transcript}`;
        // linkTag.setAttribute("href", webLink);
        // linkTag.click();

    };

    // start recognition
    recognition.start();
}