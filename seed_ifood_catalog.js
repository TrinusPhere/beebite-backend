const fs = require('fs');

const jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL2JpdGVmbG93LmFwaSIsInN1YiI6ImFkbWluIiwiZ3JvdXBzIjpbIkFETUlOIl0sInVzZXJJZCI6IjAwMDAwMDAwLTAwMDAtMDAwMC0wMDAwLTAwMDAwMDAwMDAwMiIsInN0b3JlSWQiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJuYW1lIjoiQWRtaW5pc3RyYWRvciIsInBlcm1pc3Npb25zIjpbImluZ3JlZGllbnRlcyIsImVudHJlZ2Fkb3JlcyIsInByb2R1dG9zIiwiZmluYW5jZWlybyIsImFjZXJ0b19lbnRyZWdhZG9yZXMiLCJwZWRpZG9zIiwiY2xpZW50ZXMiLCJ1c3VhcmlvcyIsImRhc2hib2FyZCIsImludGVncmFjb2VzIl0sImlhdCI6MTc5MDcyNDQ3OCwiZXhwIjoxNzkwODEwODc4LCJqdGkiOiI4OTg1NmQ3ZS01NDE5LTRiZWYtYTlmNC1hOGNkMDc2NDhlZWYifQ.wgR8kyevkEWBy6XoZPJYZtQcxL4uo0Po0Ep9L4UJhOuff-3niVosuixvO-ba7krYpmItveAhtY0kdbDvfap_IpLqWisMG4poWdGcFXTz-KIF_WBOC2XoxTh0mcNY8ykmCQ7Mu6tF6hy4bM1sPDug11uBPG1zN7GxTXPmIgRWNunVFaVZ7y48us0TjyYcKMpWyZ7bq7IGE9OfCzkW6FvTOARkriO4LzlC5LFBFFyBEfRW0eblUNjVu6Kwm0XzPWlz0cpWNaiFd28EWWcoJKobJr3FDn5EnLYhp8HqMnKNydfOq791XLSOF-9y6eva2SVSPVrlUo8x1ZYG0SkNU0dzag";
const headers = { 'Content-Type': 'application/json', 'Cookie': `biteflow_jwt=${jwt}` };
const BASE_URL = 'http://localhost:9090/api/v1';

async function request(method, path, body = null) {
    const opts = { method, headers: { ...headers } };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(`${BASE_URL}${path}`, opts);
    if (!res.ok) {
        const err = await res.text();
        throw new Error(`HTTP ${res.status} em ${path}: ${err}`);
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

async function run() {
    try {
        console.log("--- LIMPANDO CARDÁPIO ANTIGO VIA API ---");

        const prods = await request('GET', '/products?size=1000');
        if (prods && prods.content) for (const p of prods.content) await request('DELETE', `/products/${p.id}`).catch(() => { });

        const cats = await request('GET', '/categories');
        if (cats && cats.content) for (const c of cats.content) await request('DELETE', `/categories/${c.id}`).catch(() => { });

        const groups = await request('GET', '/modifier-groups');
        if (groups && groups.content) for (const g of groups.content) await request('DELETE', `/modifier-groups/${g.id}`).catch(() => { });

        console.log("\n--- CRIANDO NOVO CARDÁPIO PARA IFOOD ---");

        // 1. Criar Categoria
        console.log("1. Criando Categoria 'Lanches Especiais'...");
        const category = await request('POST', '/categories', {
            name: "Lanches Especiais",
            isActive: true,
            displayOrder: 1
        });
        console.log(`   -> Categoria criada: ${category.id}`);

        // 2. Criar Produto Secundário (que será usado como Adicional)
        console.log("2. Criando Produto que será um Adicional (Bacon Extra)...");
        const baconProduct = await request('POST', '/products', {
            name: "Bacon Extra",
            categoryId: category.id,
            description: "Porção generosa de bacon crocante",
            price: 5.00,
            stock: 100,
            externalCode: `PDV-BACON-${Date.now()}`,
            catalogItemType: "DEFAULT",
            technicalSheet: [],
            removableItemIds: [],
            modifierGroupIds: [],
            channelPrices: []
        });
        console.log(`   -> Produto Adicional criado: ${baconProduct.id}`);

        // 3. Criar Grupo de Modificadores e vincular à opção do Bacon
        console.log("3. Criando Grupo de Modificadores (Escolha seus Adicionais)...");
        const modGroup = await request('POST', '/modifier-groups', {
            name: "Turbine seu Lanche",
            type: "CHECKBOX",
            required: false,
            minSelections: 0,
            maxSelections: 3,
            semanticType: "INGREDIENTS",
            options: [
                {
                    name: "Bacon Extra",
                    price: 5.00,
                    sortOrder: 1,
                    linkedProductId: baconProduct.id
                }
            ]
        });
        console.log(`   -> Grupo criado: ${modGroup.id}`);

        // 4. Criar Produto Principal e vincular o Grupo de Modificadores
        console.log("4. Criando Produto Principal (X-Bacon Mestre)...");
        const mainProduct = await request('POST', '/products', {
            name: "X-Bacon Mestre",
            categoryId: category.id,
            description: "Hambúrguer de 200g com muito queijo e bacon",
            price: 29.90,
            stock: 50,
            externalCode: `PDV-XBACON-MESTRE-${Date.now()}`,
            catalogItemType: "DEFAULT",
            modifierGroupIds: [modGroup.id],
            technicalSheet: [],
            removableItemIds: [],
            channelPrices: []
        });
        console.log(`   -> Produto Principal criado: ${mainProduct.id}`);

        console.log("\n✅ CARDÁPIO DE TESTE GERADO COM SUCESSO!");
        console.log("Agora você pode ir pro Passo 2 e disparar o Export pro iFood!");

    } catch (error) {
        console.error("❌ ERRO:", error.message);
    }
}

run();
