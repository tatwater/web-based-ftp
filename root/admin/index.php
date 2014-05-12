<!doctype html>
<?php
  session_start();

  $userinfo = array(
    'admin'=>'password');
  $server_running = false;
  $port = "0000";
  $bad_username = false;
  $bad_password = false;
  $bad_port = false;
  $print_run = false;
  $print_off = false;

  // Checks submitted username and password, and saves to session if valid
  if(isset($_POST['username'])) {
    if(!array_key_exists($_POST['username'], $userinfo)) {
      $bad_username = true;
    }
    else {
      if($userinfo[$_POST['username']] == $_POST['password']) {
        $_SESSION['username'] = $_POST['username'];
      }
      else {
        $bad_password = true;
      }
    }
  }

  // Logs the user out
  if(isset($_GET['logout'])) {
    $_SESSION['username'] = '';
    header('Location: ' . $_SERVER['PHP_SELF']);
  }

  // Grabs server status
  if(isset($_POST['running'])) {
    if($_POST['running'] == "t") {
      $server_running = true;
    }
    else if($_POST['running'] == "f") {
      $server_running = false;
    }
  }

  // Takes in a port number; if 0000 starts server, otherwise stops server
  if(isset($_POST['port'])) {
    if($server_running == false) {
      $server_running = true;
      $port = $_POST['port'];
      $print_run = true;
    }
    else {
      $server_running = false;
      $port = "0000";
      $print_off = true;
    }
  }
?>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="author" content="Teagan Atwater" />
    <meta name="viewport" content="initial-scale=1.0, width=device-width" />
    <link href="reset.css" rel="stylesheet" />
    <link href="style.css" rel="stylesheet" />
    <link href="fonts/font-awesome/css/font-awesome.min.css" rel="stylesheet">
    <!--[if lt IE 9]><script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
    <title>FTP Control Panel</title>
    <script>
      // Make sure all login fields are filled in before submitting
      function checkFilledLogin() {
        var u = document.forms["login"]["username"].value;
        var p = document.forms["login"]["password"].value;

        if(u == null || u == "") {
          document.getElementById('error').innerHTML = "Please enter a username.";
          return false;
        }
        else if(p == null || p == "") {
          document.getElementById('error').innerHTML = "Please enter a password.";
          return false;
        }
        else {
          return true;
        }
      }

      //Make sure all control fields are filled before submitting
      function checkFilledControls() {
        var p = document.forms["controls"]["port"].value;
        var r = document.forms["controls"]["running"].value;

        console.log(r);
        if(p == null || p == "") {
          document.getElementById('error2').innerHTML = "Please enter a port number.";
          return false;
        }
        else {
          return true;
        }
      }

      // Called onload if incorrect username
      function badUsername() {
        document.getElementById('error').innerHTML = "The username you entered was incorrect.";
      }

      // Called onload if incorrect password
      function badPassword() {
        document.getElementById('error').innerHTML = "The password you entered was incorrect.";
      }

      // Called onload if invalid port number
      function badPort() {
        document.getElementById('error2').innerHTML = "Please enter a valid port number.";
      }

      function printRun() {
        console.log("running");
        setTimeout(function() {
          console.log("end");
        }, 1000);
      }

      function printOff() {
        console.log("offline");
        setTimeout(function() {
          console.log("end");
        }, 1000);
      }
    </script>
  </head>
  <body <?php if($bad_username) { echo "onload='badUsername();'"; } else if($bad_password) { echo "onload='badPassword();'"; } else if($print_run) { echo "onload='printRun();'"; } else if($print_off) { echo "onload='printOff();'"; } ?>>
    <section>
      <h1>FTP Control Panel</h1>
<?php
  if(!$_SESSION['username']) {
?>
      <p>To make changes to the FTP server, please log in.</p>
      <form name="login" method="post" action="index.php" onsubmit="return checkFilledLogin()">
        <div class="half">
          <input name="username" type="text" <?php if(!$_SESSION['username']) { echo "autofocus='autofocus'"; } ?> placeholder="Username" />
        </div>
        <div class="spacer"></div>
        <div class="half">
          <input name="password" type="password" placeholder="Password" />
        </div>
        <br />
        <input type="submit" value="Log In" />
        <p id="error"></p>
      </form>
<?php
  }
  else {
    echo "  <p>Welcome, <strong>", $_SESSION['username'], "</strong>!</p>";
?>
      <form class="secondary" name="controls" method="post" action="index.php" onsubmit="return checkFilledControls();">
        <input name="running" type="text" class="hide" value="<?php if($server_running) { echo "t"; } else { echo "f"; } ?>" />
        <input name="port" type="number" min="1" <?php if($server_running) { echo "class='hide' value='$port'"; } else { echo "autofocus='autofocus' placeholder='Port Number'"; } ?> />
<?php
    if($server_running && $port) {
      echo "<input name='portcover' type='text' placeholder='Port Number: $port' disabled />";
    }
?>
        <br />
        <input type="submit" value="<?php if(!$server_running) { echo "Start Server"; } else { echo "Stop Server"; } ?>" />
        <a class="logout" href="?logout">Log Out</a>
        <p id="error2"></p>
      </form>
<?php
  }
?>
    </section>
  </body>
</html>