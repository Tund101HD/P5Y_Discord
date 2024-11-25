# P5Y Discord Bot

## P5Y Discord bot? What is that?

The P5Y Discord Bot is supposed to make clan wars squad building easier by automatically moving registered users into 
the correct teams and channels. A squad leader can set all important information, such as min-activity or battle rating, at the beginning
of the session, and the bot will handle everything else. Users that enter the waiting channel will be tracked automatically and moved into the
corresponding channels as soon as a space frees itself. Additionally, the bot aids in objective decision-making to select the most fit player
for a squad, measured by past performance, activity, attended training session and more.

## What information does the bot track?

+ Player activity in clan wars - How many active rounds did you play?
+ Readiness for clan wars - How long did you actively wait or hold yourself ready?
+ Player activity in training sessions - How many training sessions did you attend?
+ Player rating in battles - How good did you perform in clan wars battles=
+ Player data - What is your preferred battle rating, what units do you play, etc...

All of this enables the bot to make the most optimal decision in choosing a fitting player for the squad to ensure a high success rate
in our battles and a balanced level of play for you.

## Usage
### For squad leaders:
+ How can I start a session?
> /startsession **(br)*** (min-activity) (min-priority) (exclude_users)

+ How can I fill a session with different conditions?
>/fillsession (min-activity) (min-priority) (exclude_users)

+ How can I move members between sessions?
> /move **(User)*** **(session_id)***

+ How can I end my session?
> /endsession
    
+ How do I list all active sessions? 
> /listsessions

+ How to I get to be squad 1?
> The early bird catches the worm. Be the first to start a session!

All options defined more closely:
    
    br: The battle rating that the session will be playing on
    min-activity: The minimum activity, that a member needs to automatically join
    min-priority: The minimum priority, that a member needs to join
    exclude_users: Comma-Seperated-List (User1,User2,UserN...) of banned users
> [!NOTE]
> The bot will automatically notify users saved in the database, if they fit the session, and it's not filled up!

> [!NOTE]
> Bold options that are marked with a *, are **_not_** optional!

> [!TIP]
> Common inputs like all battle ratings can be autocompleted.

### For squad members:
+ How can I register myself, to start playing?
> /register

After you send this command, please follow instructions in your DMs!

+ How do I join a session?
>/join 

Please remember that usually you should be moved automatically if you fit a session, and you were waiting already. The command
is meant for users that weren't waiting when the session(s) was/were created. It is possible you will be set on a waiting list.

+ When does the bot track me as waiting?
> As soon as you enter the channel "Warteraum" you will be tracked as waiting. Alternatively you can use
> `/join` if you are in a different channel.

> [!NOTE]
> As long as you're not deafened, you move into the AFK-Channel, and you are in a voice chat, you will keep being tracked as 
> waiting, even though you left the waiting channel. That means that you can confidently move into different channels. You will
> be notified when you get set on or removed from the waiting list.

+ How can I check my data?
> /me


+ How can I change the information I entered during registration?
> Just enter `/register` again.

Please follow the instructions in your DMS again!

+ How do I leave a session?
> You can leave a session, by simply leaving voice chat. If you are an active member, you can join the waiting channel to
> free up your position and set yourself onto the waiting list. You will be blocked from search for five minutes.

> [!CAUTION]
> If you are added to the waiting list, you should be ready to start playing at all times. The bot will move fitting players automatically into
> a corresponding channel without asking for permission. Please make sure, you are reachable for the entire time you're waiting, and you can start
> playing as soon as you get moved.

### For admins:

+ How can I set the ids for roles and channels?
> /setid **(type)*** **(id)***

    Types:
        - Squad1-Ground (Voicechannel)
        - Squad1-Air (Voicechannel)
        - Squad2-Ground (Voicechannel)
        - Squad2-Air (Voicechannel)
        - Waitingroom (Voicechannel)
        - AFK (Voicechannel)
        - CW-Role (Role)
        - SL-Role (Role)

## What data does the bot save?

The bot keeps track of for evaluation important data in its database, for later use. This means that the bot will save different
data about you, to fulfill his task. More precisely, the bot saves:

+ Active playtime as a squad member (If you are part of a squad)
+ Time spent actively waiting to join a squad (If you're officially marked as waiting)
+ Information about participated training sessions (How often, which training, date, battle rating, etc...)
+ Circumstantially the topic of chat messages in the clan wars channel (ex. @Kilian I'm ready!)
+ All information, that you provided during registration
+ Player performance in training sessions and played matches measured by in-game stats
+ Your current voice channel (If you are officially tracked as waiting or participating)

The bot does ***not*** save this information:

+ What you say, stream or play in channels during waiting for participation
+ Exact messages in the clan wars channel (The bot only cares what you are planning to do, ex. ready or not?)
+ Non-relevant information that doesn't contribute to the success of matches and session building, for example, general activity in chats or subscription status
+ Automatically refreshed information about your stats (ex. via Thunderskill Api)

> [!IMPORTANT]
> By registering you take notice and accept of these terms


## How far along is the development of the bot?

- [x] Registering and saving of users
- [x] Internal session management 
- [x] /startsession, /fillsession, /listsessions
- [ ] /endsession, /move
- [ ] /join, /me, /setid
- [ ] Finalising user tracking and session management
- [ ] Finalising the database
- [ ] Image recognition and OCR model for parsing in-game stat tables
- [ ] Code documentation and refactoring (The boring stuff)

## Ideas, wishes or found a bug?

Please DM me directly under `Tund_101_HD` or speak to Kilian (`SlatanKiliankowitz`)
Alternatively, you can open an issue on this GitHub repo: https://github.com/Tund101HD/P5Y_Discord/issues/new