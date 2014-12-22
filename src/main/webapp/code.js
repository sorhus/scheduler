$(function(){ // on dom ready

var cy = cytoscape({
container: document.getElementById('cy'),

style: [
{
selector: 'node',
css: {
'content': 'data(id)',
'text-valign': 'center',
'text-halign': 'center'
}
},
{
selector: '$node > node',
css: {
'padding-top': '10px',
'padding-left': '10px',
'padding-bottom': '10px',
'padding-right': '10px',
'text-valign': 'top',
'text-halign': 'center'
}
},
{
selector: 'edge',
css: {
'target-arrow-shape': 'triangle'
}
},
{
selector: ':selected',
css: {
'background-color': 'black',
'line-color': 'black',
'target-arrow-color': 'black',
'source-arrow-color': 'black'
}
}
],

//elements: {
//nodes: [
//{ data: { id: 'a', parent: 'b' } },
//{ data: { id: 'b' } },
//{ data: { id: 'c', parent: 'b' } },
//{ data: { id: 'd' } },
//{ data: { id: 'e' } },
//{ data: { id: 'f', parent: 'e' } }
//],
//edges: [
//{ data: { id: 'ad', source: 'a', target: 'd' } },
//{ data: { id: 'eb', source: 'e', target: 'b' } }
//]
//},
elements: {
    nodes: [
        { data: { id: 'job_a', parent: 'job_a' }},
        { data: { id: 'job_b' }},
        { data: { id: 'job_b-foo', parent: 'job_b' }},
        { data: { id: 'job_b-bar', parent: 'job_b' }},
        { data: { id: 'job_c', parent: 'job_c' }}
    ],
    edges: [
        { data: { id: 'ab', source: 'job_a', target: 'job_b' }},
        { data: { id: 'ac', source: 'job_a', target: 'job_c' }},
        { data: { id: 'bc', source: 'job_b', target: 'job_c' }}
    ]
},


layout: {
name: 'cose',
padding: 5
}
});

}); // on dom ready
