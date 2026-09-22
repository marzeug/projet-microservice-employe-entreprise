/* Une seule scène plein écran, sans dépendance et sans capture des interactions. */
(() => {
  const mouvementReduit = window.matchMedia('(prefers-reduced-motion: reduce)');
  let arreter = () => {};

  window.celebrerFavori = (bouton) => {
    arreter();
    if (mouvementReduit.matches || document.hidden) return;

    const canvas = document.createElement('canvas');
    canvas.className = 'favori-celebration';
    canvas.setAttribute('aria-hidden', 'true');
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    document.body.appendChild(canvas);

    let largeur, hauteur, frame;
    const origine = bouton.getBoundingClientRect();
    const depart = { x: origine.left + origine.width / 2, y: origine.top + origine.height / 2 };
    const couleurs = ['#fff3bd', '#ffd166', '#ffac33', '#ffffff', '#70e5da'];
    const ajuster = () => {
      largeur = window.innerWidth;
      hauteur = window.innerHeight;
      const ratio = Math.min(window.devicePixelRatio || 1, 2);
      canvas.width = Math.round(largeur * ratio);
      canvas.height = Math.round(hauteur * ratio);
      ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
    };
    ajuster();

    // Plusieurs bouquets occupent le centre et les quatre coins du viewport.
    const bouquets = [
      { x: 0.5, y: 0.43, debut: 0.6, nombre: 100 },
      { x: 0.16, y: 0.25, debut: 0.95, nombre: 45 },
      { x: 0.84, y: 0.22, debut: 1.12, nombre: 45 },
      { x: 0.2, y: 0.72, debut: 1.3, nombre: 40 },
      { x: 0.8, y: 0.7, debut: 1.45, nombre: 40 },
    ];
    const particules = bouquets.flatMap((bouquet) =>
      Array.from({ length: bouquet.nombre }, (_, index) => ({
        ...bouquet,
        angle: Math.random() * Math.PI * 2,
        vitesse: 0.12 + Math.random() * 0.4,
        taille: 2 + Math.random() * 5,
        couleur: couleurs[index % couleurs.length],
        rotation: Math.random() * Math.PI,
        etoile: index % 4 === 0,
        duree: 1.3 + Math.random() * 0.9,
      }))
    );

    const etoile = (x, y, taille, rotation, couleur) => {
      ctx.save();
      ctx.translate(x, y);
      ctx.rotate(rotation);
      ctx.beginPath();
      for (let i = 0; i < 10; i++) {
        const angle = i * Math.PI / 5 - Math.PI / 2;
        const rayon = taille * (i % 2 ? 0.46 : 1);
        const px = Math.cos(angle) * rayon;
        const py = Math.sin(angle) * rayon;
        if (i === 0) ctx.moveTo(px, py);
        else ctx.lineTo(px, py);
      }
      ctx.closePath();
      ctx.fillStyle = couleur;
      ctx.fill();
      ctx.restore();
    };

    arreter = () => {
      cancelAnimationFrame(frame);
      canvas.remove();
      window.removeEventListener('resize', ajuster);
      window.removeEventListener('pagehide', arreter);
      document.removeEventListener('visibilitychange', visibilite);
      mouvementReduit.removeEventListener('change', preference);
    };
    const visibilite = () => { if (document.hidden) arreter(); };
    const preference = () => { if (mouvementReduit.matches) arreter(); };
    window.addEventListener('resize', ajuster);
    window.addEventListener('pagehide', arreter);
    document.addEventListener('visibilitychange', visibilite);
    mouvementReduit.addEventListener('change', preference);

    const debut = performance.now();
    const dessiner = (maintenant) => {
      const temps = (maintenant - debut) / 1000;
      if (temps >= 4) { arreter(); return; }
      ctx.clearRect(0, 0, largeur, hauteur);
      const cx = largeur / 2;
      const cy = hauteur * 0.43;
      const dimension = Math.hypot(largeur, hauteur);
      const sortie = Math.min(1, (4 - temps) / 0.8);

      // Voile cinématique progressif, sans flash, laissant la page visible.
      ctx.globalAlpha = Math.min(1, temps / 0.45) * sortie;
      const fond = ctx.createRadialGradient(cx, cy, 0, cx, cy, dimension * 0.65);
      fond.addColorStop(0, 'rgba(69, 49, 24, 0.32)');
      fond.addColorStop(1, 'rgba(10, 22, 38, 0.68)');
      ctx.fillStyle = fond;
      ctx.fillRect(0, 0, largeur, hauteur);

      // Deux vagues lumineuses traversent toute la page.
      for (let i = 0; i < 2; i++) {
        const age = temps - 0.55 - i * 0.24;
        if (age < 0 || age > 1.6) continue;
        ctx.globalAlpha = (1 - age / 1.6) * 0.65 * sortie;
        ctx.strokeStyle = '#ffd779';
        ctx.lineWidth = 3 - age;
        ctx.beginPath();
        ctx.arc(cx, cy, dimension * age * 0.65, 0, Math.PI * 2);
        ctx.stroke();
      }

      // Trajectoires avec traînées puis pluie de confettis et petites étoiles.
      for (const p of particules) {
        const age = temps - p.debut;
        if (age < 0 || age > p.duree) continue;
        const course = (1 - Math.exp(-age * 1.8)) * dimension * p.vitesse;
        const x = p.x * largeur + Math.cos(p.angle) * course;
        const y = p.y * hauteur + Math.sin(p.angle) * course + age * age * 70;
        ctx.globalAlpha = Math.min(1, age * 12) * Math.pow(1 - age / p.duree, 0.6) * sortie;
        ctx.strokeStyle = p.couleur;
        ctx.lineWidth = p.taille * 0.35;
        ctx.beginPath();
        ctx.moveTo(x - Math.cos(p.angle) * 18, y - Math.sin(p.angle) * 18 - age * 5);
        ctx.lineTo(x, y);
        ctx.stroke();
        if (p.etoile) etoile(x, y, p.taille * 1.5, p.rotation + age * 2, p.couleur);
        else {
          ctx.save();
          ctx.translate(x, y);
          ctx.rotate(p.rotation + age * 4);
          ctx.fillStyle = p.couleur;
          ctx.fillRect(-p.taille / 2, -p.taille, p.taille, p.taille * 2);
          ctx.restore();
        }
      }

      // L'étoile quitte le bouton, grandit au centre puis se dissout.
      const voyage = Math.min(1, temps / 0.65);
      const ease = 1 - Math.pow(1 - voyage, 3);
      const x = depart.x + (cx - depart.x) * ease;
      const y = depart.y + (cy - depart.y) * ease - Math.sin(voyage * Math.PI) * hauteur * 0.15;
      const rayon = Math.min(largeur * 0.23, hauteur * 0.19, 145);
      const rebond = 1 + Math.sin(Math.max(0, temps - 0.65) * 13) * Math.exp(-Math.max(0, temps - 0.65) * 4) * 0.2;
      ctx.globalAlpha = Math.max(0, Math.min(1, (2.9 - temps) / 0.55));
      ctx.shadowColor = '#ffc34b';
      ctx.shadowBlur = 35;
      const or = ctx.createLinearGradient(x - rayon, y - rayon, x + rayon, y + rayon);
      or.addColorStop(0, '#fff9da');
      or.addColorStop(0.45, '#ffda76');
      or.addColorStop(1, '#e89520');
      etoile(x, y, (13 + (rayon - 13) * ease) * rebond, ease * Math.PI * 2, or);
      ctx.shadowBlur = 0;

      if (temps > 0.8) {
        ctx.globalAlpha = Math.min(1, (temps - 0.8) / 0.3) * Math.max(0, Math.min(1, (3.1 - temps) / 0.6));
        ctx.fillStyle = '#fff5d6';
        ctx.textAlign = 'center';
        ctx.font = `600 ${Math.min(32, largeur * 0.065)}px "Segoe UI", sans-serif`;
        ctx.fillText('Un favori de plus !', cx, cy + rayon + 60, largeur - 32);
      }
      ctx.globalAlpha = 1;
      frame = requestAnimationFrame(dessiner);
    };
    frame = requestAnimationFrame(dessiner);
  };
})();
