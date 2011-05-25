$(window).load(function(){
    var fw = 100;        //fixed width
    var fh = 100;        //fixed height
    var sl = 'img.screen_shot'; //selector
    $(sl).each(function(){
      var w = $(this).attr("width");
      var h = $(this).attr("height");
      if (w >= h) {
        $(this).width(fw);
      } else {
       $(this).height(fh);
      }

    });
});

$(document).ready(function(){
    $('select').change(function(ev) {
      var target_id = $(this).attr("id");
      if ( target_id == "select_project" ){
      $("#select_class").val("");
      $("#select_device").val("");
      $("#select_date").val("");
      } else if ( target_id == "select_class" ){
      $("#select_device").val("");
      $("#select_date").val("");
      } else if ( target_id == "select_device" ){
      $("#select_date").val("");
      }
      $("#button_search").click();
      });


    });


