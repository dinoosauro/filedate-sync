const fs = require("fs");
const path = require("path");
let str = "";
fs.readdirSync(process.argv[2], { recursive: true }).forEach((file) => {
    if (!file.startsWith(".")) {
        try {
            const stat = fs.statSync(`${process.argv[2]}${process.argv[2].endsWith(path.sep) ? "" : path.sep}${file}`);
            if (!stat.isDirectory()) str += `${stat.mtime.getTime()} ${file}\n`;
        } catch (ex) {
            console.warn(ex);
        }
    }
});
fs.writeFileSync(`${process.argv[2].substring(process.argv[2].lastIndexOf("/") + 1)}.txt`, str.substring(0, str.length - 1));