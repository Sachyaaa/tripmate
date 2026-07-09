# Starts the local MySQL 8.4 you already have installed — NO admin, NO Windows service.
# Leave this window open while you work; press Ctrl+C to stop MySQL.
# Data lives in C:\Users\sdivase\tripmate-mysql-data  (delete that folder to reset the DB).

$mysqld  = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
$datadir = "C:\Users\sdivase\tripmate-mysql-data"

Write-Host "Starting MySQL 8.4 on localhost:3306 ..." -ForegroundColor Green
Write-Host "DB=tripmate  root pass=Pass@123  (also user 'tripmate'/'tripmate_pass')" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop." -ForegroundColor Yellow
& $mysqld --datadir="$datadir" --port=3306 --console
