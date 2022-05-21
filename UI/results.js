let desc = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

let links = [{ url: "https://dmoz-odp.org/", description: desc }, { url: "https://www.python.org/", description: desc }, { url: "https://www.bbc.com/", description: desc }, { url: "https://www.reddit.com/", description: desc }, { url: "https://www.nytimes.com/", description: desc }, { url: "https://archive.org/web/", description: desc }];
for (let i = 0; i < links.length; i++) {
    let newlink = document.getElementById("link").cloneNode(true);
    newlink.setAttribute("id", `link-${i}`);
    // newlink.setAttribute("hre", `link-${i}`);
    newlink.classList.remove("hide");
    newlink.children[0].children[0].textContent = links[i].url;
    newlink.children[0].children[0].setAttribute("href", links[i].url);
    newlink.children[1].children[0].textContent = links[i].description;
    document.body.appendChild(newlink);

}