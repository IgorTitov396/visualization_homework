width = 932;

tree = data => {
    const root = d3.hierarchy(data);
    root.dx = 10;
    root.dy = width / (root.height + 1);
    return d3.tree().nodeSize([root.dx, root.dy])(root);
};

data = JSON.parse(document.getElementById("graph").getAttribute("data"));

const root = tree(data);

let x0 = Infinity;
let x1 = -x0;
root.each(d => {
    if (d.x > x1) x1 = d.x;
    if (d.x < x0) x0 = d.x;
});


const svg = d3.select("#svgcontainer")
    .append("svg")
    .attr("width", x1 - x0 + root.dx * 2)
    .style("width", "100%")
    .style("height", "auto")
    .style("height", "100%");

const g = svg.append("g")
    .attr("font-family", "sans-serif")
    .attr("font-size", 10)
    .attr("transform", `translate(${root.dy / 3},${root.dx - x0})`);

const link = g.append("g")
    .attr("fill", "none")
    .attr("stroke", "#555")
    .attr("stroke-opacity", 0.4)
    .attr("stroke-width", 1.5)
    .selectAll("path")
    .data(root.links())
    .enter().append("path")
    .attr("d", d3.linkHorizontal()
        .x(d => d.y)
        .y(d => d.x));

const node = g.append("g")
    .attr("stroke-linejoin", "round")
    .attr("stroke-width", 3)
    .selectAll("g")
    .data(root.descendants().reverse())
    .enter().append("g")
    .attr("transform", d => `translate(${d.y},${d.x})`);

node.append("circle")
    .attr("fill", d => d.children ? "#555" : "#999")
    .attr("r", 2.5);

node.append("text")
    .attr("dy", "0.31em")
    .attr("x", d => d.children ? -6 : 6)
    .attr("text-anchor", d => d.children ? "end" : "start")
    .text(d => d.data.name)
    .clone(true).lower()
    .attr("stroke", "white");

svg.node();

getHeight = () => {
    const points = g.node().children[1].children;
    let maxY = Number.MIN_SAFE_INTEGER;
    let minY = Number.MAX_SAFE_INTEGER;
    for (point of points) {
        const pointY = parseInt(point.getAttribute("transform").replace(/[^\d,.]/g, '').split(",")[1]);
        if (pointY > maxY) maxY = pointY;
        if (pointY < minY) minY = pointY;
    }
    return (maxY - minY) + 50
};
getWidth = () => {
    const points = g.node().children[1].children;
    let maxX = Number.MIN_SAFE_INTEGER;
    let minX = Number.MAX_SAFE_INTEGER;
    for (point of points) {
        const pointX = parseInt(point.getAttribute("transform").replace(/[^\d,.]/g, '').split(",")[0]);
        if (pointX > maxY) maxY = pointX;
        if (pointX < minY) minY = pointX;
    }
    return (maxY - minY) + 20
};
svg.style("height", getHeight() * 1.5);
