@echo off
set address=175.112.14.251
set port=3260
set main=pc080
for /f "usebackq tokens=4" %%S in (`iscsicli SessionList ^|find /i "¼¼¼Ç ID"`) do (iscsicli LogoutTarget %%S)