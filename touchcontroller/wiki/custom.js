(() => {
    const template = [/*FOOTER TEMPLATE HERE*/];
    if (template.length == 0) {
        return;
    }
    const content = document.getElementById("mdbook-content");
    const footer = document.createElement("footer");
    footer.innerHTML = template[0];
    content.appendChild(footer);
})();
