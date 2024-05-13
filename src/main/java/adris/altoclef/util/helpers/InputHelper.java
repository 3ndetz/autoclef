package adris.altoclef.util.helpers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class InputHelper {

    public static boolean isKeyPressed(int code) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), code);
    }
}
/*
3 -- Cancel
8 -- Backspace
9 -- Tab
10 -- Enter
12 -- Clear
16 -- Shift
17 -- Ctrl
18 -- Alt
19 -- Pause
20 -- Caps Lock
21 -- Kana
24 -- Final
25 -- Kanji
27 -- Escape
28 -- Convert
29 -- No Convert
30 -- Accept
31 -- Mode Change
32 -- Space
33 -- Page Up
34 -- Page Down
35 -- End
36 -- Home
37 -- Left
38 -- Up
39 -- Right
40 -- Down
44 -- Comma
45 -- Minus
46 -- Period
47 -- Slash
48 -- 0
49 -- 1
50 -- 2
51 -- 3
52 -- 4
53 -- 5
54 -- 6
55 -- 7
56 -- 8
57 -- 9
59 -- Semicolon
61 -- Equals
65 -- A
66 -- B
67 -- C
68 -- D
69 -- E
70 -- F
71 -- G
72 -- H
73 -- I
74 -- J
75 -- K
76 -- L
77 -- M
78 -- N
79 -- O
80 -- P
81 -- Q
82 -- R
83 -- S
84 -- T
85 -- U
86 -- V
87 -- W
88 -- X
89 -- Y
90 -- Z
91 -- Open Bracket
92 -- Back Slash
93 -- Close Bracket
96 -- NumPad-0
97 -- NumPad-1
98 -- NumPad-2
99 -- NumPad-3
100 -- NumPad-4
101 -- NumPad-5
102 -- NumPad-6
103 -- NumPad-7
104 -- NumPad-8
105 -- NumPad-9
106 -- NumPad *
107 -- NumPad +
108 -- NumPad ,
109 -- NumPad -
110 -- NumPad .
111 -- NumPad /
112 -- F1
113 -- F2
114 -- F3
115 -- F4
116 -- F5
117 -- F6
118 -- F7
119 -- F8
120 -- F9
121 -- F10
122 -- F11
123 -- F12
127 -- Delete
128 -- Dead Grave
129 -- Dead Acute
130 -- Dead Circumflex
131 -- Dead Tilde
132 -- Dead Macron
133 -- Dead Breve
134 -- Dead Above Dot
135 -- Dead Diaeresis
136 -- Dead Above Ring
137 -- Dead Double Acute
138 -- Dead Caron
139 -- Dead Cedilla
140 -- Dead Ogonek
141 -- Dead Iota
142 -- Dead Voiced Sound
143 -- Dead Semivoiced Sound
144 -- Num Lock
145 -- Scroll Lock
150 -- Ampersand
151 -- Asterisk
152 -- Double Quote
153 -- Less
154 -- Print Screen
155 -- Insert
156 -- Help
157 -- Meta
160 -- Greater
161 -- Left Brace
162 -- Right Brace
192 -- Back Quote
222 -- Quote
224 -- Up
225 -- Down
226 -- Left
227 -- Right
240 -- Alphanumeric
241 -- Katakana
242 -- Hiragana
243 -- Full-Width
244 -- Half-Width
245 -- Roman Characters
256 -- All Candidates
257 -- Previous Candidate
258 -- Code Input
259 -- Japanese Katakana
260 -- Japanese Hiragana
261 -- Japanese Roman
262 -- Kana Lock
263 -- Input Method On/Off
512 -- At
513 -- Colon
514 -- Circumflex
515 -- Dollar
516 -- Euro
517 -- Exclamation Mark
518 -- Inverted Exclamation Mark
519 -- Left Parenthesis
520 -- Number Sign
521 -- Plus
522 -- Right Parenthesis
523 -- Underscore
524 -- Windows
525 -- Context Menu
61440 -- F13
61441 -- F14
61442 -- F15
61443 -- F16
61444 -- F17
61445 -- F18
61446 -- F19
61447 -- F20
61448 -- F21
61449 -- F22
61450 -- F23
61451 -- F24
65312 -- Compose
65368 -- Begin
65406 -- Alt Graph
65480 -- Stop
65481 -- Again
65482 -- Props
65483 -- Undo
65485 -- Copy
65487 -- Paste
65488 -- Find
65489 -- Cut





 */