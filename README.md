# Firefds Kit [UDC]

Xposed module for Samsung U (Android 14) devices.

## Features
The module has the following features:
- Fake system status to Official
- Custom advanced power menu options:
  - Power off
  - Restart
  - Emergency mode
  - Recovery
  - Download
  - Data mode switch
  - Screenshot
  - Switch User (when multi user is enabled)
  - SystemUI restart
  - Flashlight
  - Screen Recorder (requires Samsung screen recorder app installed)
- Disable restart confirmation
- Enable call recording
- Replace add call button instead of call recording
- Skip tracks with volume buttons
- Enable call recording from menu
- Auto call recording
- Hide VoLTE icon in status bar
- Hide persistent USB connection notification
- Hide persistent charging notification
- Enable block phrases in messages app settings
- Enable native blur on notification panel pull down
- Enable multi user toggle
- Set max user value selector
- Show seconds in status bar clock toggle
- Show clock date on right of clock toggle
- Add date to status bar clock options
- Enable biometrics and fingerprints unlock on reboot toggle
- Add network speed menu to show network speed in the status bar
- Data icon symbol selection (4G, LTE, 4G+, 4.5G)
- Show Data usage view in quick panel
- Double tap for sleep
- Hide NFC icon
- Disable Bluetooth toggle popup
- Disable sync toggle popup
- Disable high level brightness poup
- Hide carrier label
- Carrier label size selection
- Disable loud volume warning
- Disable volume control sound
- Disable low battery sound
- Screen timeout settings
- NFC behavior settings
- Auto MTP
- Disable camera temperature check
- Enable camera shutter sound menu
- Disable call number formatting
- Disable SMS to MMS threshold
- Force MMS connect
- Bypass exchange security (currently not working)
- Disable signature check
- Disable secure flag

## Attention
**THERE COULD BE BUGS/CRASHES/BOOTLOOPS**, but it's pretty stable.
Please upload any xposed logs when you encounter any issue. I can't help you without the logs!
Confirmed working on:
- Galaxy S21 FE

## Installation

To install this module you need the following apps and modules installed on your device:
1. Magisk v26.0 and above - https://github.com/topjohnwu/Magisk/releases

### Option 1 - Zygisk - Recommended
1. LSPosed Magisk Zygisk Release module v1.9.2 and above - https://github.com/LSPosed/LSPosed/releases

### Option 2 - Riru
1. LSPosed Magisk module v1.9.2 and above - https://github.com/LSPosed/LSPosed/releases
2. Riru Magisk module v26.1.7 and above - https://github.com/RikkaApps/Riru/releases


## Known Issues

- Some features are removed on purpose. Since GravityBox has been working on Samsung devices for a while without much issues, I only implemented features that need special Samsung coding. You can check You can check GravityBox for R, when it will become available, for additional features.
- Double tap for sleep not working.
- Data icon symbol selection not working.

## External Libraries

The project uses the following libraries:
1. https://github.com/rovo89/XposedBridge
2. https://github.com/rovo89/XposedMods/tree/master/XposedLibrary
3. Samsung framework libraries which are used for compile only

## Credits
This module wouldn't have been here without the following people:
- The people behind [LSPosed](https://github.com/LSPosed/LSPosed) for their amazing work!
- [RikkaW](https://github.com/RikkaApps) - Creator of Riru Magisk module, which provides a way to inject codes into zygote process
- [rovo89](https://github.com/rovo89) - Creator of the original Xposed framework APIs
- [solohsu](https://github.com/solohsu) and [MlgmXyysd](https://github.com/MlgmXyysd) - Creators of the EdXposed Magisk module and Installer that made all of this possible
- [C3C0](https://github.com/GravityBox) - Creator of GravityBox Xposed modules, which I learnt a lot from
- [Wanam](https://github.com/wanam) - Creator of the original XTouchWiz module, which this module is based on.
- [topjohnwu](https://github.com/topjohnwu) - Creator of Magisk
- [AbrahamGC](https://forum.xda-developers.com/member.php?u=7393522) - [For the Extended Power Menu - Pie - Odex framework Smali guide](https://forum.xda-developers.com/showpost.php?p=78910083&postcount=944)
- Big thank you to [m8980](https://forum.xda-developers.com/m/m8980.1614889) and [ianmacd](https://forum.xda-developers.com/m/ianmacd.7187684) for testing countless versions and sending xposed logs

This is a moded version of Wanam's XTouchWiz:
https://github.com/wanam/XTouchWiz

## Telegram
Announcements and pre release versions - https://t.me/firefdskit
