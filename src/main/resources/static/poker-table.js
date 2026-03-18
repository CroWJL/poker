/**
 * 牌桌组件：椭圆形德州扑克桌，绿台面，座位环桌分布
 * 用法：PokerTable.mount(container).render(tableState, me)
 */
(function (window) {
    const STYLE_ID = 'poker-table-component-styles';

    // 椭圆形桌子周围座位位置（外圈百分比，相对外层 wrap），不含“我”（我固定底部中央外圈）
    // 顺时针：右下、右、右上、顶、左上、左、左下
    const SEAT_POSITIONS = [
        { left: '76%', top: '92%' },   // 0 底部偏右（更贴近）
        { left: '90%', top: '70%' },   // 1 右下
        { left: '90%', top: '36%' },   // 2 右上
        { left: '50%', top: '8%' },    // 3 顶部
        { left: '10%', top: '36%' },   // 4 左上
        { left: '10%', top: '70%' },   // 5 左下
        { left: '24%', top: '92%' }    // 6 底部偏左
    ];

    function injectStyles() {
        if (document.getElementById(STYLE_ID)) return;
        const style = document.createElement('style');
        style.id = STYLE_ID;
        style.textContent = `
.poker-table {
    --felt: #0f6b2d;
    --felt-dark: #084318;
    --rail: #1a1a1a;
    --rail-border: #333;
    --panel: rgba(0, 0, 0, 0.5);
    --panel-border: rgba(255, 255, 255, 0.1);
    --accent: #e8a317;
    position: relative;
    width: min(980px, 100%);
    aspect-ratio: 2 / 1;
    /* 给牌/座位更多空间：桌面优先占满可用高度 */
    height: min(62vh, 520px);
    max-height: 520px;
    min-height: 340px;
    border-radius: 50%;
    background: radial-gradient(ellipse 80% 70% at 50% 50%, #138f3e 0%, #0a5522 50%, #063d18 100%);
    border: clamp(10px, 1.6vmin, 16px) solid var(--rail);
    box-shadow: 
        0 0 0 2px var(--rail-border),
        inset 0 2px 8px rgba(255,255,255,0.08),
        0 12px 32px rgba(0,0,0,0.5);
    box-sizing: border-box;
}
.poker-table-wrap {
    position: relative;
    display: grid;
    place-items: center;
    width: 100%;
    /* 外圈更贴近桌边：进一步收紧预留空间 */
    padding: clamp(20px, 3.4vmin, 38px) clamp(18px, 3.2vmin, 36px) clamp(30px, 5.2vmin, 56px);
    box-sizing: border-box;
}
.poker-table__inner {
    position: absolute;
    inset: clamp(10px, 1.8vmin, 18px);
    border-radius: 50%;
    border: 2px solid rgba(0,0,0,0.25);
    pointer-events: none;
}
.poker-table__community-wrap {
    position: absolute;
    left: 50%;
    top: 42%;
    transform: translate(-50%, -50%);
    display: flex;
    gap: clamp(6px, 0.9vmin, 10px);
    flex-wrap: nowrap;
    justify-content: center;
    align-items: center;
    pointer-events: auto;
}
.poker-table__community {
    display: flex;
    flex-wrap: nowrap;
    gap: clamp(6px, 0.9vmin, 10px);
    justify-content: center;
    align-items: center;
}
.poker-table__community-empty {
    font-size: 12px;
    color: rgba(255,255,255,0.4);
    pointer-events: none;
}
.poker-table__seats {
    position: absolute;
    inset: 0;
    pointer-events: none;
}
.poker-table__seat {
    position: absolute;
    transform: translate(-50%, -50%);
    min-width: clamp(108px, 12vmin, 150px);
    padding: clamp(6px, 0.9vmin, 10px) clamp(10px, 1.2vmin, 14px);
    background: var(--panel);
    border: 1px solid var(--panel-border);
    border-radius: 12px;
    text-align: center;
    font-size: clamp(12px, 1.4vmin, 14px);
    pointer-events: auto;
    box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}
.poker-table__seat-name {
    font-weight: 600;
    margin-bottom: 2px;
    word-break: break-all;
}
.poker-table__seat-chip {
    font-size: clamp(11px, 1.25vmin, 13px);
    opacity: 0.9;
}
.poker-table__seat-cards {
    display: flex;
    gap: clamp(4px, 0.8vmin, 8px);
    justify-content: center;
    margin-top: clamp(6px, 0.9vmin, 10px);
}
.poker-table__me-wrap {
    position: absolute;
    left: 50%;
    bottom: 0%;
    transform: translateX(-50%);
    pointer-events: auto;
}
.poker-table__me {
    min-width: clamp(150px, 18vmin, 220px);
    padding: clamp(8px, 1.1vmin, 12px) clamp(12px, 1.4vmin, 18px);
    background: var(--panel);
    border: 2px solid var(--accent);
    border-radius: 14px;
    text-align: center;
    font-size: clamp(13px, 1.5vmin, 15px);
    box-shadow: 0 0 16px rgba(232,163,23,0.4), 0 4px 12px rgba(0,0,0,0.35);
}
.poker-table__me-name {
    font-weight: 700;
    margin-bottom: 4px;
}
.poker-table__me-cards {
    display: flex;
    gap: clamp(6px, 1vmin, 10px);
    justify-content: center;
    flex-wrap: wrap;
}
.poker-table .card {
    box-shadow: 0 2px 6px rgba(0,0,0,0.4);
    border: 1px solid rgba(0,0,0,0.15);
    border-radius: 6px;
}
.poker-table .card.card-small {
    transform: scale(1.08);
    transform-origin: center;
}
.poker-table .card.card-large {
    transform: scale(1.06);
    transform-origin: center;
}
.poker-table .card-inner {
    background: linear-gradient(145deg, #fff 0%, #f0f0f0 100%);
    border-radius: 4px;
}
.poker-table .card-back .card-inner {
    background: linear-gradient(145deg, #1a5a8a 0%, #0d3d5c 100%);
    border: 1px solid #0a2d45;
}
`;
        document.head.appendChild(style);
    }

    function createCard(code, opts) {
        if (window.CardUI && window.CardUI.createCardElement) {
            return window.CardUI.createCardElement(code, opts);
        }
        const div = document.createElement('div');
        div.className = 'card';
        div.textContent = code || '';
        return div;
    }

    function mount(container) {
        injectStyles();

        const root = document.createElement('div');
        root.className = 'poker-table-wrap';
        root.innerHTML =
            '<div class="poker-table" data-role="table">' +
            '<span class="poker-table__inner" aria-hidden="true"></span>' +
            '<div class="poker-table__community-wrap" data-role="community-wrap">' +
            '<div class="poker-table__community" data-role="community"></div>' +
            '<span class="poker-table__community-empty" data-role="community-empty">等待发牌</span>' +
            '</div>' +
            '</div>' +
            '<div class="poker-table__seats" data-role="seats"></div>' +
            '<div class="poker-table__me-wrap" data-role="me-wrap">' +
            '<div class="poker-table__me" data-role="me"></div>' +
            '</div>';

        container.appendChild(root);

        const el = {
            table: root.querySelector('[data-role="table"]'),
            community: root.querySelector('[data-role="community"]'),
            communityEmpty: root.querySelector('[data-role="community-empty"]'),
            seats: root.querySelector('[data-role="seats"]'),
            meWrap: root.querySelector('[data-role="me-wrap"]'),
            me: root.querySelector('[data-role="me"]')
        };

        function render(tableState, me) {
            const players = tableState.players || [];
            const community = tableState.communityCards || [];
            const selfHole = tableState.selfHoleCards || [];

            el.community.innerHTML = '';
            if (community.length === 0) {
                el.communityEmpty.style.display = 'inline';
            } else {
                el.communityEmpty.style.display = 'none';
                community.forEach(function (c) {
                    el.community.appendChild(createCard(c, { size: 'large' }));
                });
            }

            el.seats.innerHTML = '';
            const others = players.filter(function (p) {
                return !me || p.userId !== me.userId;
            });
            const maxSeats = SEAT_POSITIONS.length;
            others.slice(0, maxSeats).forEach(function (p, index) {
                const pos = SEAT_POSITIONS[index];
                const seat = document.createElement('div');
                seat.className = 'poker-table__seat';
                seat.style.left = pos.left;
                seat.style.top = pos.top;
                const name = document.createElement('div');
                name.className = 'poker-table__seat-name';
                name.textContent = p.nickname;
                const chip = document.createElement('div');
                chip.className = 'poker-table__seat-chip';
                chip.textContent = '筹码 ' + p.chipBalance;
                const cardsBox = document.createElement('div');
                cardsBox.className = 'poker-table__seat-cards';
                for (let i = 0; i < 2; i++) {
                    cardsBox.appendChild(createCard('X♠', { size: 'small', facedown: true }));
                }
                seat.appendChild(name);
                seat.appendChild(chip);
                seat.appendChild(cardsBox);
                el.seats.appendChild(seat);
            });

            el.me.innerHTML = '';
            const mePlayer = players.find(function (p) {
                return me && me.userId === p.userId;
            });
            if (mePlayer) {
                el.meWrap.style.display = '';
                const n = document.createElement('div');
                n.className = 'poker-table__me-name';
                n.textContent = mePlayer.nickname;
                const ch = document.createElement('div');
                ch.textContent = '筹码 ' + mePlayer.chipBalance;
                const cardsBox = document.createElement('div');
                cardsBox.className = 'poker-table__me-cards';
                (selfHole || []).forEach(function (code) {
                    cardsBox.appendChild(createCard(code, { size: 'large' }));
                });
                el.me.appendChild(n);
                el.me.appendChild(ch);
                el.me.appendChild(cardsBox);
            } else {
                el.meWrap.style.display = 'none';
            }
        }

        function destroy() {
            root.remove();
        }

        return { render: render, destroy: destroy, root: root };
    }

    window.PokerTable = { mount: mount };
})(window);
