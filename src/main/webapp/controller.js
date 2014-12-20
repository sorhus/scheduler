$(document).ready(function(){
  $("p").bind("click",function(){
    alert("The paragraph was clicked.");
  });
});

$(document).ready(function(){
  $("button").bind("click",function(){
    $.getJSON("pipe/status", "name=test-pipe", function(data){
      alert("data " + data);
    });
  });
});
