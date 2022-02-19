import os
os.system('pip install wmi')
import wmi
c = wmi.WMI()
wql = "Select * From Win32_USBControllerDevice"
print('Connected devices: ')
print('----------------------------------------')
for item in c.query(wql):
    print(item.Dependent.Caption)
    print("----------------------------------------")