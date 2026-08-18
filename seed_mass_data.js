const fs = require('fs');

const jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL2JpdGVmbG93LmFwaSIsInN1YiI6ImFkbWluIiwiZ3JvdXBzIjpbIkFETUlOIl0sInVzZXJJZCI6IjAwMDAwMDAwLTAwMDAtMDAwMC0wMDAwLTAwMDAwMDAwMDAwMiIsInN0b3JlSWQiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJuYW1lIjoiQWRtaW5pc3RyYWRvciIsInBlcm1pc3Npb25zIjpbImluZ3JlZGllbnRlcyIsImVudHJlZ2Fkb3JlcyIsInByb2R1dG9zIiwiZmluYW5jZWlybyIsImFjZXJ0b19lbnRyZWdhZG9yZXMiLCJwZWRpZG9zIiwiY2xpZW50ZXMiLCJ1c3VhcmlvcyIsImRhc2hib2FyZCIsImludGVncmFjb2VzIl0sImlhdCI6MTc4NzAxNTA3MywiZXhwIjoxNzg3MTAxNDczLCJqdGkiOiJmNGM5MDdhZi02ODM1LTQxNTQtOTc3MC1lOTZlZDljNGI2YzIifQ.de5QTACOv4rr1KkTBgoe7wjNZ0qobm2sTZuSvUzrENUJ0xDqNlGz0jLTdaMEfQB8CtAL-a98lNigTatxieqOCP8FgBBvr7pukbI4Rw24JZFLSUDeit9J2_iFRAw6opjsG6z99uelnW1q-pTQBF5wJyKUhWBTVahsmMHd6JrKyn-3jy5yGMf43FuyJ3Qt0Ud_5ROii0kZE198o9aKx2NKAaZ2WadZ-G5GqJQ0am1bXaiY5kkjvR5Y5TJJ75vUICFiJUcmq01dGuvrPiIs46QjmQUv0wQlYZMzHW9sSrFUWoQ599OaAzxDMzlUqb-laCdA1FsacmEfOhXCCeCX24G5ew";
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

const rand = (arr) => arr[Math.floor(Math.random() * arr.length)];
const randInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

async function clearOldData() {
    console.log("--- LIMPANDO DADOS ANTIGOS VIA API ---");
    const prods = await request('GET', '/products?size=1000');
    if (prods && prods.content) for (const p of prods.content) await request('DELETE', `/products/${p.id}`).catch(() => {});
    const cats = await request('GET', '/categories');
    if (cats && cats.content) for (const c of cats.content) await request('DELETE', `/categories/${c.id}`).catch(() => {});
    const ings = await request('GET', '/ingredients?size=1000');
    if (ings && ings.content) for (const i of ings.content) await request('DELETE', `/ingredients/${i.id}`).catch(() => {});
    const delivs = await request('GET', '/drivers?size=1000');
    if (delivs && delivs.content) for (const d of delivs.content) await request('DELETE', `/drivers/${d.id}`).catch(() => {});
    const custs = await request('GET', '/customers?size=1000');
    if (custs && custs.content) for (const c of custs.content) await request('DELETE', `/customers/${c.id}`).catch(() => {});
    console.log("Limpeza concluída! (Pedidos novos serão adicionados aos antigos, criando histórico rico).");
}

async function seed() {
    try {
        await clearOldData();

        console.log("\n--- CRIANDO CATEGORIAS ---");
        const catBurgers = await request('POST', '/categories', { name: "Hambúrgueres", displayOrder: 1 });
        const catPizzas = await request('POST', '/categories', { name: "Pizzas", displayOrder: 2 });
        const catHotDog = await request('POST', '/categories', { name: "Cachorro Quente", displayOrder: 3 });
        const catChurras = await request('POST', '/categories', { name: "Churrasco", displayOrder: 4 });
        const catBebidas = await request('POST', '/categories', { name: "Bebidas", displayOrder: 5 });
        console.log("Categorias criadas.");

        console.log("\n--- CRIANDO INGREDIENTES ---");
        const ingsToCreate = [
            { name: "Pão Brioche", unit: "UN", costPrice: 1.50 },
            { name: "Carne Bovina 150g", unit: "UN", costPrice: 4.00 },
            { name: "Queijo Cheddar", unit: "KG", costPrice: 40.00 },
            { name: "Bacon em Fatias", unit: "KG", costPrice: 50.00 },
            { name: "Massa de Pizza", unit: "UN", costPrice: 3.00 },
            { name: "Molho de Tomate", unit: "L", costPrice: 10.00 },
            { name: "Mussarela Rala", unit: "KG", costPrice: 35.00 },
            { name: "Calabresa Fatiada", unit: "KG", costPrice: 28.00 },
            { name: "Salsicha", unit: "UN", costPrice: 0.80 },
            { name: "Pão de Hot Dog", unit: "UN", costPrice: 0.50 },
            { name: "Batata Palha", unit: "KG", costPrice: 18.00 },
            { name: "Picanha", unit: "KG", costPrice: 85.00 },
            { name: "Linguiça Toscana", unit: "KG", costPrice: 22.00 },
            { name: "Pão de Alho", unit: "UN", costPrice: 1.20 }
        ];
        const ingredients = [];
        for (const i of ingsToCreate) ingredients.push(await request('POST', '/ingredients', i));
        console.log("Ingredientes criados.");

        console.log("\n--- CRIANDO ADICIONAIS & MODIFICADORES ---");
        const addBacon = await request('POST', '/additionals', { name: "Extra Bacon", category: "Extras", price: 4.00, active: true });
        const addCheddar = await request('POST', '/additionals', { name: "Cheddar Cremoso", category: "Extras", price: 3.50, active: true });
        const addPure = await request('POST', '/additionals', { name: "Purê de Batata", category: "Extras", price: 2.50, active: true });
        
        const modPonto = await request('POST', '/modifier-groups', {
            name: "Ponto da Carne", type: "RADIO", minSelections: 1, maxSelections: 1,
            options: [{ name: "Ao Ponto", price: 0 }, { name: "Bem Passada", price: 0 }, { name: "Mal Passada", price: 0 }]
        });
        const modExtrasBurger = await request('POST', '/modifier-groups', {
            name: "Turbine seu Lanche", type: "CHECKBOX", minSelections: 0, maxSelections: 3,
            options: [{ name: "Extra Bacon", price: 4.00 }, { name: "Cheddar Cremoso", price: 3.50 }]
        });
        const modExtrasDog = await request('POST', '/modifier-groups', {
            name: "Turbine seu Dogão", type: "CHECKBOX", minSelections: 0, maxSelections: 3,
            options: [{ name: "Purê de Batata", price: 2.50 }, { name: "Extra Bacon", price: 4.00 }]
        });
        console.log("Modificadores criados.");

        console.log("\n--- CRIANDO PRODUTOS ---");
        const productsToCreate = [
            {
                name: "X-Burger Clássico", categoryId: catBurgers.id, price: 25.00, description: "O clássico irresistível.",
                technicalSheet: [ { ingredientId: ingredients[0].id, quantity: 1 }, { ingredientId: ingredients[1].id, quantity: 1 }, { ingredientId: ingredients[2].id, quantity: 0.05 } ],
                modifierGroupIds: [modPonto.id, modExtrasBurger.id]
            },
            {
                name: "X-Bacon Supremo", categoryId: catBurgers.id, price: 32.00, description: "Muito bacon!",
                technicalSheet: [ { ingredientId: ingredients[0].id, quantity: 1 }, { ingredientId: ingredients[1].id, quantity: 2 }, { ingredientId: ingredients[2].id, quantity: 0.05 }, { ingredientId: ingredients[3].id, quantity: 0.1 } ],
                modifierGroupIds: [modPonto.id, modExtrasBurger.id]
            },
            {
                name: "Pizza Calabresa (Grande)", categoryId: catPizzas.id, price: 55.00, description: "Massa fina e crocante.",
                technicalSheet: [ { ingredientId: ingredients[4].id, quantity: 1 }, { ingredientId: ingredients[5].id, quantity: 0.2 }, { ingredientId: ingredients[6].id, quantity: 0.3 }, { ingredientId: ingredients[7].id, quantity: 0.4 } ],
                modifierGroupIds: []
            },
            {
                name: "Dogão Paulistano", categoryId: catHotDog.id, price: 18.00, description: "Com purê e batata palha.",
                technicalSheet: [ { ingredientId: ingredients[9].id, quantity: 1 }, { ingredientId: ingredients[8].id, quantity: 2 }, { ingredientId: ingredients[10].id, quantity: 0.05 } ],
                modifierGroupIds: [modExtrasDog.id]
            },
            {
                name: "Churrasco Picanha (Porção)", categoryId: catChurras.id, price: 89.90, description: "Acompanha pão de alho e farofa.",
                technicalSheet: [ { ingredientId: ingredients[11].id, quantity: 0.5 }, { ingredientId: ingredients[12].id, quantity: 0.2 }, { ingredientId: ingredients[13].id, quantity: 2 } ],
                modifierGroupIds: [modPonto.id]
            },
            {
                name: "Coca-Cola 2L", categoryId: catBebidas.id, price: 12.00, description: "Gelada",
                technicalSheet: [], modifierGroupIds: []
            },
            {
                name: "Suco de Laranja 500ml", categoryId: catBebidas.id, price: 8.00, description: "Natural",
                technicalSheet: [], modifierGroupIds: []
            }
        ];
        const products = [];
        for (const p of productsToCreate) products.push(await request('POST', '/products', p));
        console.log(`Foram criados ${products.length} produtos.`);

        console.log("\n--- CRIANDO CLIENTES ---");
        const customersToCreate = [
            { name: "João Silva", phone: "11999999991", addresses: [{ street: "Rua A", number: "123", neighborhood: "Centro", city: "SP", state: "SP", zipCode: "01000000" }] },
            { name: "Maria Oliveira", phone: "11999999992", addresses: [{ street: "Rua B", number: "45", neighborhood: "Jardins", city: "SP", state: "SP", zipCode: "01400000" }] },
            { name: "Carlos Souza", phone: "11999999993", addresses: [{ street: "Av Paulista", number: "1000", neighborhood: "Bela Vista", city: "SP", state: "SP", zipCode: "01310100" }] },
            { name: "Ana Beatriz", phone: "11999999994", addresses: [{ street: "Rua Augusta", number: "500", neighborhood: "Consolação", city: "SP", state: "SP", zipCode: "01305000" }] },
            { name: "Pedro Paulo", phone: "11999999995", addresses: [{ street: "Rua Funchal", number: "100", neighborhood: "Vila Olímpia", city: "SP", state: "SP", zipCode: "04551000" }] }
        ];
        const customers = [];
        for (const c of customersToCreate) customers.push(await request('POST', '/customers', c));
        console.log("Clientes criados.");

        console.log("\n--- CRIANDO ENTREGADORES ---");
        const deliverersToCreate = [
            { name: "Motoboy Zé", phone: "11988888881", vehicleType: "MOTO", plate: "ABC1234", active: true },
            { name: "Motoboy Lucas", phone: "11988888882", vehicleType: "MOTO", plate: "XYZ9876", active: true },
            { name: "Motoboy Tiago", phone: "11988888883", vehicleType: "MOTO", plate: "QWE4321", active: true }
        ];
        const deliverers = [];
        for (const d of deliverersToCreate) deliverers.push(await request('POST', '/drivers', d));
        console.log("Entregadores criados.");

        console.log("\n--- SIMULANDO OPERAÇÃO: CRIANDO PEDIDOS (150 PEDIDOS) ---");
        const NUM_ORDERS = 150;
        
        for (let i = 1; i <= NUM_ORDERS; i++) {
            const type = rand(["DELIVERY", "DELIVERY", "DELIVERY", "BALCAO", "BALCAO"]); // 60% delivery
            const customer = (type === "DELIVERY") ? rand(customers) : null;
            
            const items = [];
            const numItems = randInt(1, 4);
            for(let j=0; j<numItems; j++) {
                const prod = rand(products);
                let mods = [];
                if (prod.modifierGroupIds && prod.modifierGroupIds.length > 0) {
                    if (prod.name.includes("Burger") || prod.name.includes("Churrasco")) mods.push({ modifierGroupId: modPonto.id, optionName: "Ao Ponto", quantity: 1, price: 0 });
                }
                items.push({ productId: prod.id, quantity: randInt(1, 2), selectedModifiers: mods, notes: "" });
            }

            let custInfo = null;
            if (type === "DELIVERY" && customer) custInfo = { id: customer.id, name: customer.name, address: `${customer.address}, ${customer.number} - ${customer.neighborhood}` };
            else if (type === "BALCAO") custInfo = customer ? { id: customer.id, name: customer.name, address: "" } : null;

            const orderReq = { type: type, customer: custInfo, paymentMethod: rand(["PIX", "CREDITO", "DEBITO", "DINHEIRO"]), items: items, deliveryCost: type === "DELIVERY" ? 5.00 : 0 };
            const order = await request('POST', '/orders', orderReq);
            
            const fate = Math.random();
            if (fate < 0.05) {
                // 5% Cancelado
                await request('PATCH', `/orders/${order.id}/move`, { targetStatus: "CANCELADO" }).catch(e=>console.log(e));
            } else if (fate < 0.15) {
                // 10% Fica em Analise (não faz nada)
            } else if (fate < 0.25) {
                // 10% Fica em Preparo
                await request('PATCH', `/orders/${order.id}/move`, { targetStatus: "PREPARO" }).catch(e=>console.log(e));
            } else if (fate < 0.35 && type === "DELIVERY") {
                // 10% Fica em Rota
                await request('PATCH', `/orders/${order.id}/move`, { targetStatus: "PREPARO" }).catch(e=>console.log(e));
                const deliv = rand(deliverers);
                await request('PATCH', `/orders/${order.id}/assign-driver`, { driverId: deliv.id }).catch(e=>console.log(e));
                await request('PATCH', `/orders/${order.id}/move`, { targetStatus: "EM_ROTA" }).catch(e=>console.log(e));
            } else {
                // O resto (60%+) Finaliza!
                await request('PATCH', `/orders/${order.id}/move`, { targetStatus: "PREPARO" }).catch(e=>console.log(e));
                if (type === "DELIVERY") {
                    const deliv = rand(deliverers);
                    await request('PATCH', `/orders/${order.id}/assign-driver`, { driverId: deliv.id }).catch(e=>console.log(e));
                    await request('PATCH', `/orders/${order.id}/move`, { targetStatus: "EM_ROTA" }).catch(e=>console.log(e));
                }
                await request('PATCH', `/orders/${order.id}/move`, { targetStatus: "FINALIZADO" }).catch(e=>console.log(e));
            }
            process.stdout.write(`\rPedidos processados: ${i}/${NUM_ORDERS}`);
        }

        console.log(`\n\n--- ESPALHANDO PEDIDOS NO TEMPO (TIME TRAVEL) ---`);
        await request('POST', '/dev/spread-orders');
        console.log("Os 150 pedidos foram magicamente distribuídos pelos últimos 7 dias na base de dados!");

        console.log(`\n=== MASSA DE DADOS CONCLUÍDA! ===`);
    } catch (err) {
        console.error("\nErro no script:", err);
    }
}

seed();
