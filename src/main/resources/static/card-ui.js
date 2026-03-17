(function (window) {
    const SUIT_COLOR_CLASS = {
        '♠': 'card-suit-black',
        '♣': 'card-suit-black',
        '♥': 'card-suit-red',
        '♦': 'card-suit-red'
    };

    function parseCode(code) {
        if (!code || typeof code !== 'string') {
            return { rank: '?', suit: '♠' };
        }
        code = code.trim();
        const suit = code.slice(-1);
        const rank = code.slice(0, -1);
        return { rank, suit };
    }

    function createCardElement(code, options) {
        const { rank, suit } = parseCode(code);
        const opt = options || {};

        const root = document.createElement('div');
        root.className = 'card card-ui';
        if (opt.size === 'small') {
            root.classList.add('card-small');
        } else if (opt.size === 'large') {
            root.classList.add('card-large');
        }

        const inner = document.createElement('div');
        inner.className = 'card-inner';
        root.appendChild(inner);

        const cornerTop = document.createElement('div');
        cornerTop.className = 'card-corner card-corner-top';
        cornerTop.textContent = rank + suit;

        const center = document.createElement('div');
        center.className = 'card-center';
        center.textContent = suit;

        const cornerBottom = document.createElement('div');
        cornerBottom.className = 'card-corner card-corner-bottom';
        cornerBottom.textContent = rank + suit;

        const colorClass = SUIT_COLOR_CLASS[suit] || 'card-suit-black';
        cornerTop.classList.add(colorClass);
        center.classList.add(colorClass);
        cornerBottom.classList.add(colorClass);

        inner.appendChild(cornerTop);
        inner.appendChild(center);
        inner.appendChild(cornerBottom);

        if (opt.facedown) {
            root.classList.add('card-back');
            inner.textContent = '';
        }

        return root;
    }

    window.CardUI = {
        createCardElement
    };
})(window);

