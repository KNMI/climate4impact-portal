$( document ).ready(function() {
    var loadJSON = function(){
    var httpCallback = function(data){
      if(data.error){
        
        $("#c4i_userinfo").html(data.error+"<br/>Code "+data.statuscode);
        return;
      }
      var html="";
      html+="Active users: "+data.numusers+"<br/>";
      html+="C4I sessions: "+data.numsessions+"<br/>";
      html+="Tomcat sessions: "+data.numactivetomcatsessions+"<br/>";
        
      html+="<table class=\"drupal\"><tr><th>#</th><th>Id</th><th>Basket</th><th>Jobs</th><th>Sessions</th></tr>";
      console.log(data);
      for(var j=0;j<data.users.length;j++){
        html+="<tr><td>"+(j+1)+"</td><td><span class='c4i-admin-takeuser' name='"+data.users[j].id+"'>"+data.users[j].id+((data.users[j].email==undefined)?"":("</span><br/>"+data.users[j].email))+"</td><td>"+data.users[j].basketsize+"</td><td>"+data.users[j].processingjobsize+"</td>";
        html+="<td>";
        
          html+="<table class=\"drupal c4i-admin-table-smallheader\"><tr><th>#</th><th>created</th><th>accessed</th><th>user agent</th><th>host</th><th>type</th><th>hits</th></tr>";
          for(var s=0;s<data.users[j].sessions.length;s++){
            var session = data.users[j].sessions[s];
            html+="<tr><td>"+(s+1)+"</td><td>"+
            session.created+"</td><td>"+
            session.accessed+"</td><td>"+
            session.useragent+"</td><td>"+
            session.host+"</td><td>"+
            session.sessiontype+((session.token==undefined)?"":("/<br/>"+session.token))+"</td><td>"+
            session.hits+"</td></tr>";
          }
          html+="</table>"
        html+="</td>";
        html+="</tr>";
      }
      html+="</table>"
      $("#c4i_userinfo").html(html);
      $(".c4i-admin-takeuser").attr('onclick','').click(function(t){
        console.log($(this).attr("name"));
        
        var httpCallback = function(){
          window.location.href = "/impactportal/account/login.jsp";
        };
        
        $.ajax({
          url: "/impactportal/ImpactService?service=admin&request=login&client_id="+$(this).attr("name"),
          crossDomain:true,
          dataType:"jsonp"
        }).done(function(d) {
          httpCallback(d)
        }).fail(function() {
          //alert("fail 154");
          console.log("Ajax call failed: "+url);
          httpCallback({"error":"Request failed for "+url});
        });
        
      });
      window.setTimeout(loadJSON, 5000);
      
    };
    
    $.ajax({
      url: "/impactportal/ImpactService?service=admin&request=getusers",
      crossDomain:true,
      dataType:"jsonp"
    }).done(function(d) {
      httpCallback(d)
    }).fail(function() {
      //alert("fail 154");
      console.log("Ajax call failed: "+url);
      httpCallback({"error":"Request failed for "+url});
    });
  };
  loadJSON();
});