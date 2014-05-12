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

    include("session.php");
     
    if(isset($_GET['id'])){
      $id=$_GET['id'];
      }
       
    $logged = $_SESSION['user'];
    $result = mysql_query("SELECT * FROM users WHERE username='$logged'",$link);
    $row=mysql_fetch_array($result, MYSQL_ASSOC);  
    $role=$row['role'];
    $log_id = $row['id']; 
    $user = $row['username'];
  
  //AUTOR
  if($role==1) { ?>
  
      
    <?php
              $result = mysql_query("SELECT * FROM locations WHERE id=$id",$link);
              
              while($row=mysql_fetch_array($result, MYSQL_ASSOC)){  
                $a_id=$row['id'];
                
                $time = $row['time'];
                $range = $row['distance'];
                $steps = $row['steps'];
                $av_speed = $row['speed'];
                $run_id = $row['run_id'];
              
              }
              
              $i=0; 
              $events = mysql_query("SELECT * FROM locations WHERE run_id=$run_id AND nick='$user' ORDER BY id ASC",$link); 
              
              while($row2=mysql_fetch_array($events, MYSQL_ASSOC)){  
                 //echo $row2['x'].", ".$row2['y']." ".$row2['time']."<br>";
                 $lat[$i] = $row2['lat'];
                 $lng[$i] = $row2['lng'];
                 $d_time[$i] = $row2['time'];
  	             $speed[$i] = $row2['speed'];
                 $i++;
		 
		 $r_soucet+= $row2['speed'];
		 $r_pocet++; 
              
              }

		$r_prum = ($r_soucet / $r_pocet) *(36/10);
              
              for($j=1; $j<$i; $j++){
                                
                $osa_x[$j] = millisToHours($d_time[$j]);
                $osa_y[$j] = $speed[$j]*(36/10);
              
              }
              
    ?>
    
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = google.visualization.arrayToDataTable([
          ['Time', 'Speed (km/h)'],

          <?php
            for($k=1; $k<$j; $k++){
              $a = $osa_x[$k];
              $b = $osa_y[$k];
	      
              echo "['$a', $b ],";
            }  
          ?>
        ]);

        var options = {
          title: '',
          //curveType: 'function',
          //legend: { position: 'bottom' },
          chartArea:{width:"90%",height:"75%"}
          
        };
        

        var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
        chart.draw(data, options);
      }
    </script> 
    
    <h3>Activity</h3>
    <button type="button" class="btn btn-primary">Time <strong><br><?php echo prevodCasu($time); ?></strong></button>
    <button type="button" class="btn btn-success">Distance <strong><br><?php printf('%02d m', $range); ?></strong></button>
    <button type="button" class="btn btn-info">Steps <strong><br><?php echo $steps; ?> </strong></button>
    <button type="button" class="btn btn-danger">Average speed <strong><br><?php printf('%01.2f km/h', $r_prum); ?> </strong></button>
    <hr>
    
    <h3>Graph</h3>
    <div style="border:thin solid lightgrey;"><div id="chart_div" style="width: 100%; height: 250px"></div> </div>
    
    <?php echo "<h3>Map <a href = 'map.php?id=$run_id&user=$user' target='_blank' class='btn btn-success btn-sm'><span class='glyphicon glyphicon-zoom-in'></span> Full screen</a> </h3>";
     echo "<iframe border=0 src='map.php?id=$run_id&user=$user' width='100%' height='400'></iframe>";  ?>
   

<?php 
  }
  
    function gps_distance($lat1, $lng1, $lat2, $lng2) {
    static $great_circle_radius = 6372.795;
    return acos(
        cos(deg2rad($lat1))*cos(deg2rad($lng1))*cos(deg2rad($lat2))*cos(deg2rad($lng2))
        + cos(deg2rad($lat1))*sin(deg2rad($lng1))*cos(deg2rad($lat2))*sin(deg2rad($lng2))
        + sin(deg2rad($lat1))*sin(deg2rad($lat2))
    ) * $great_circle_radius;
}

 function prevodCasu( $input )
 {

  $uSec = $input % 1000;
  $input = floor($input / 1000);

  $seconds = $input % 60;
  $input = floor($input / 60);

  $minutes = $input % 60;
  $input = floor($input / 60);

  $hours = $input % 24;
  $input = floor($input / 24);  

	printf('%02d:%02d:%02d ', $hours, $minutes, $seconds);

	//return $hours.":".$minutes.":".$seconds;
 }
 
 function millisToHours( $input )
 {

  $uSec = $input % 1000;
  $input = floor($input / 1000);

  $seconds = $input % 60;
  $input = floor($input / 60);

  $minutes = $input % 60;
  $input = floor($input / 60);

  return $minutes.":".$seconds;
 }
?>