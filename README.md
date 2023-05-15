# ModSync

What if you could share you're mods quickly and easily by simply installing a mod to do all the work for you?
That mod is ModSync.

ModSync works by generating a zip file of your mods folder and then sending that to another ModSync on another minecraft instance (WIP).


ModSync is an idea given to me by [Chee-Ho](https://github.com/Artisine).

What works:
- Creates zip
- Hosts http server (on port 23682)
- Allows users to download zip file
- Generates zip file dynamically
- The log messages only in the dev enviroment (they break in a normal enviroment)

What doesn't work:
- [Multiple http connections at once](../../issues/2)
- [Download speeds are quite slow](../../issues/3)
- [Generates zip file after the first request for it, on every start](../../issues/4)
- [Zip speeds are quite slow too](../../issues/1)
- [Getting the mods.zip internally and unzipping it, and restarting the minecraft client](../../issues/5)
