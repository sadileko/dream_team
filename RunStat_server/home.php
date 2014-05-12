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
  
  if(isset($_GET['a_del'])){
    $id=$_GET['a_del'];
    mysql_query("DELETE FROM locations WHERE run_id=$id");
    }
       
    $logged = $_SESSION['user'];
    $result = mysql_query("SELECT * FROM users WHERE username='$logged'",$link);
    $row=mysql_fetch_array($result, MYSQL_ASSOC);  
    $role = $row['role'];
    $log_id = $row['id'];
    $user = $row['username'];
  
?>

<h3>History</h3>
<div class="container">
    <div class="row  custyle">
    <table class="table table-striped custab">
    <thead>
        <tr>
            <th>ID</th><th>Name</th><th>Time</th><th>Distance</th><th class="text-center">Action</th>
        </tr>
    </thead>
      
            <?php
              $result = mysql_query("SELECT MAX(id), run_id, run_type, MAX(time), MIN(time), MAX(steps), AVG(speed), MAX(distance), lat, lng FROM `locations` WHERE nick='$user' GROUP BY run_id",$link);
              
              while($row=mysql_fetch_array($result, MYSQL_ASSOC)){ 
              
              $cas = $row['MAX(time)'];  
              $a_id=$row['run_id'];
              
              echo "<tr><td>".$row['run_id']."</td><td><a href='index.php?q=graf&id=".$row['MAX(id)']."'><strong>".typBehu($row['run_type'])."</strong></a></td><td>"; prevodCasu($cas); echo "</td><td>"; printf('%01.0f m', $row['MAX(distance)']); echo "</td><td class='text-center'><a href='?q=home&a_del=$a_id' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-remove'></span> Delete</a></td></tr>"; } 
            ?>

    </table>
    </div>
</div>

<?php

  function typBehu($type){
    if ($type == 1) { return "Distance running";   }
    else if ($type == 2) { return "Time running";  }
    else { return "Basic running"; }
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

	
  
?>
