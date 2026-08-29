const DEFAULT_WEBHOOK_URL = 'https://discord.com/api/webhooks/1543302154012590180/7geg8JpjGII4EEALJ4V81a8CCSSWEO5HT7p68XzoDiLdN9GEQ1MvOZ96yzBP3dgjIi2D';

/**
 * Sends structured log embeds to Discord Webhook with automatic continuation splitting
 * for long messages exceeding Discord limits.
 */
export async function sendDiscordLog({
    title,
    description = '',
    fields = [],
    level = 'info', // 'success', 'info', 'warning', 'error'
    envUrl = null
}) {
    const webhookUrl = envUrl || DEFAULT_WEBHOOK_URL;
    if (!webhookUrl) return;

    // Color codes (Decimal)
    const COLORS = {
        success: 0x2ECC71, // Green
        info: 0x4285F4,    // Google Blue
        warning: 0xF1C40F, // Yellow
        error: 0xE74C3C     // Red
    };

    const color = COLORS[level] || COLORS.info;

    // Discord description limit is 4096 characters.
    // Safe chunk limit at 3800 to allow room for codeblocks / formatting.
    const MAX_CHUNK_SIZE = 3800;
    const descChunks = [];

    if (!description || description.length <= MAX_CHUNK_SIZE) {
        descChunks.push(description || '');
    } else {
        for (let i = 0; i < description.length; i += MAX_CHUNK_SIZE) {
            descChunks.push(description.substring(i, i + MAX_CHUNK_SIZE));
        }
    }

    const isoTimestamp = new Date().toISOString();

    for (let i = 0; i < descChunks.length; i++) {
        const isContinuation = descChunks.length > 1;
        const chunkTitle = isContinuation
            ? `${title} (Part ${i + 1}/${descChunks.length})`
            : title;

        const embed = {
            title: chunkTitle,
            description: descChunks[i] || undefined,
            color: color,
            fields: i === 0 ? fields : [], // Attach main fields only to the first embed part
            footer: {
                text: `Zenlock Security Audit Logger${isContinuation ? ` • Page ${i + 1} of ${descChunks.length}` : ''}`,
                icon_url: 'https://cdn-icons-png.flaticon.com/512/2092/2092663.png'
            },
            timestamp: isoTimestamp
        };

        const payload = {
            username: 'Zenlock Security Audit',
            avatar_url: 'https://cdn-icons-png.flaticon.com/512/2092/2092663.png',
            embeds: [embed]
        };

        try {
            await fetch(webhookUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } catch (err) {
            console.error('Failed to dispatch Discord webhook log:', err.message);
        }
    }
}
