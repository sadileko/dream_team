<?php

/***********************************************************************************************************************
*
* This file is part of the RunStat project
* ==========================================
*
* Copyright (C) 2014 by University of West Bohemia (http://www.zcu.cz/en/)
*
***********************************************************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
* an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
* specific language governing permissions and limitations under the License.
*
***********************************************************************************************************************
*
* Dream team, 2014/5/11 Jan Janouskovec
*
**********************************************************************************************************************/

  include("config.php");
  
  if(isset($_GET['id'])){
  $id=$_GET['id'];
  }
  
  if(isset($_GET['user'])){
  $user=$_GET['user'];
  }
  
  
  $i=0; 
  $events = mysql_query("SELECT * FROM locations WHERE nick='$user' AND run_id=$id ",$link); 
              
  while($row2=mysql_fetch_array($events, MYSQL_ASSOC)){  
   $lat[$i] = $row2['lat'];
   $lng[$i] = $row2['lng'];
   $i++;
  
  }

?>

<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no">
    <meta charset="utf-8">
    <title>Mapa</title>
    <style>
      html, body, #map-canvas {
        height: 100%;
        margin: 0px;
        padding: 0px
      }
    </style>
    <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
    <script>

function initialize() {
  var mapOptions = {
    zoom: 100,
    center: new google.maps.LatLng(<?php echo "$lat[0],$lng[0]"; ?>),
    mapTypeId: google.maps.MapTypeId.TERRAIN
  };

  var map = new google.maps.Map(document.getElementById('map-canvas'),
      mapOptions);

  var flightPlanCoordinates = [

    <?php
            for($j=0; $j<$i; $j++){
              $a = $lat[$j];
              $b = $lng[$j];
              echo "new google.maps.LatLng($a, $b),";
            }  
          ?>
  ];

  var flightPath = new google.maps.Polyline({
    path: flightPlanCoordinates,
    geodesic: true,
    strokeColor: '#FF0000',
    strokeOpacity: 0.6,
    strokeWeight: 3
  });

  flightPath.setMap(map);
}

google.maps.event.addDomListener(window, 'load', initialize);

    </script>
  </head>
  <body>
    <div id="map-canvas"></div>
  </body>
</html>
<noscript>