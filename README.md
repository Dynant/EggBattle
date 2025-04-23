# EggBattle

A Minecraft plugin for organizing egg battles between players.

## Commands

### Player Commands
- `/eggbattle join` - Join the Egg Battle event
- `/eggbattle leave` - Leave the Egg Battle event

### Admin Commands
All admin commands require the `eggbattle.admin` permission.

- `/eggbattle setup` - Setup the Egg Battle event
- `/eggbattle reset` - Reset the Egg Battle event

- `/eggbattle start` - Start the Egg Battle event
- `/eggbattle stop` - Stop the Egg Battle event
- `/eggbattle status` - Show the current status of the Egg Battle event

- `/eggbattle top` - Show top 10 player scores
- `/eggbattle resetscores` - Reset all player scores
- `/eggbattle resetplayer <player>` - Reset the score for a specific player

- `/eggbattle broadcast_explanation` - Broadcast the event explanation to all players
- `/eggbattle broadcast_end` - Broadcast the end message to all players

## Permissions
- `eggbattle.admin` - Grants access to all administrative commands
- No special permission is required for player commands (join/leave)
