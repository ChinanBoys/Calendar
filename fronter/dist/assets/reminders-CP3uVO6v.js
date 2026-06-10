import{r as e}from"./request-BFiYGKXx.js";function m(r=24){return e.get("/reminders/upcoming",{params:{hours:r}})}function t(r){return e.patch(`/reminders/${r}/read`)}export{m as f,t as m};
