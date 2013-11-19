var checkOpenIdCookie = function(a) {
  if (a.checked == false) {
    makeHTTPRequest("/impactportal/consumer?keepid=off");
  } else {
    makeHTTPRequest("/impactportal/consumer?keepid=on");
  }

}
$(function() {
  var name = $("#name"),

  allFields = $([]).add(name), tips = $(".validateTips");
  name.keyup(function(e) {
    if (e.keyCode == 13) {
      $('#composeidentifierbutton').click();
    }
  });
  function updateTips(t) {
    tips.text(t).addClass("ui-state-highlight");
    setTimeout(function() {
      tips.removeClass("ui-state-highlight", 1500);
    }, 500);
  }
  function checkLength(o, n, min, max) {
    if (o.val().length > max || o.val().length < min) {
      o.addClass("ui-state-error");
      updateTips("Length of " + n + " must be between " + min + " and " + max
          + ".");
      return false;
    } else {
      return true;
    }
  }
  function checkRegexp(o, regexp, n) {
    if (!(regexp.test(o.val()))) {
      o.addClass("ui-state-error");
      updateTips(n);
      return false;
    } else {
      return true;
    }
  }

  $("#dialog-form")
      .dialog(
          {
            autoOpen : false,
            height : 200,
            width : 400,
            modal : true,
            buttons : {
              "Compose identifier" : {
                text : "Compose identifier",
                id : 'composeidentifierbutton',
                click : function() {
                  var bValid = true;
                  bValid = bValid && checkLength(name, "username", 3, 16);
                  bValid = bValid
                      && checkRegexp(name, /^[a-z]([0-9a-z_.])+$/i,
                          "Username may consist of a-z, 0-9, underscores, begin with a letter.");
                  if (bValid) {
                    var dataCentreName = $(this).dialog("option")["datacentre"];
                    var username = name.val();
                    var openid = "";
                    if (dataCentreName == 'PCMDI') {
                      openid = "https://pcmdi9.llnl.gov/esgf-idp/openid/"
                          + username;
                    }
                    if (dataCentreName == 'CEDA') {
                      openid = "https://ceda.ac.uk/openid/" + username;
                    }
                    if (dataCentreName == 'DKRZ') {
                      openid = "https://esgf-data.dkrz.de/esgf-idp/openid/"
                          + username;
                    }

                    $('#openid_identifier_input').val(openid);
                    $('#login_button').click();

                    $(this).dialog("close");
                  }
                  return false;
                }
              },
              Cancel : function() {

                $(this).dialog("close");
              }

            },

            close : function() {
              allFields.val("").removeClass("ui-state-error");
            }
          });
});
var openDialog = function(datacentre) {
  $('#dialog-form').dialog("option", "datacentre", datacentre);
  $('#dialog-form').dialog('open');

}