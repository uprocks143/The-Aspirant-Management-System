const fs = require('fs');
const path = require('path');

const filePath = path.join(__dirname, 'app/src/main/java/com/example/ui/screens/DashboardScreen.kt');
const content = fs.readFileSync(filePath, 'utf8');

const lines = content.split('\n');
let level = 0;

for (let i = 0; i < lines.length; i++) {
    const origLevel = level;
    const line = lines[i];
    for (let char of line) {
        if (char === '{') level++;
        if (char === '}') level--;
    }
    if (i >= 1930 && i <= 1985) {
        console.log(`Line ${i + 1} (level ${origLevel} -> ${level}): ${line.trim()}`);
    }
}
