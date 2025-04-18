import { Link, useLocation } from "react-router-dom";

export default function LoggedOutNavbar() {

    const location = useLocation();

    return (
        <>
            <nav className="navbar navbar-expand-lg bg-123458 fixed-top">
                <div className="container-fluid">
                    <img width={50} height={50}
                        src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsTAAALEwEAmp
                        wYAAAEWElEQVR4nO1ZW4hcRRC98RUVX/GBikQRX2DABygiakQREVT8EBEF8yHir58Bf/TDD/FH93PFBwiKDCq44BKZqeqdbDJTd
                        XPd3UzX3YRBo6ASxUeCz2jiXunZ3M2dO7c7M7PLzOxuH+ifoXq66nRV9em+QeDh4TEMKKVOE4IxzTguUjojWE+Ymdl5iTAoYUxag
                        3D3fFS9PFgPiMOpWzXjN0vBHx+a8fu4jncEaxnC8LQw/JUPfokEwiMNgmeDtYZSqXSqJnjVFnhBNoxHUXR6sBYwz+WLNEG52+AzJ
                        Oxs1CuXFv1nN/ODUUCjXrlJGA4UOwlHdR1fEMLnheBfi823DVa3rUoCdIhPCuEfllr/WQjuT22lru4Wxh+LMwH+1gTbVg0BSZJs0
                        Awva4KF4uBBz1H16vw8HVY3a8YvrEERjJleMtIENGnyPM34qaOuP4ui8vm2+aLUOUL4iYOEHY3p6U0jSYBQ5UbN0LTs+oIQvJIkL52
                        S2jebkxs14ZvC+JrJmvR3Y2NsrRlkWWOoBDQIHxbCw44afiZrP1urXKEZ6hm79/LHXszwhBD+2evpMVACkiTZIIzbNeF/Fke+02H59
                        rbAqHKPJjjYmd44UavVzsraCqtbilTjSBAgi/X6sb1esSasLmub4zzyWqdDNd8j9kfqYiGYGikCpKau1YTiaHYfZHfza6XO1ATvduO4+
                        V9TItn1Wv2C8Z2RIEBYPSQMv1oWPmZKos0+Kl8pBHt620E4ENenrutY22QQw9GhEaCNclsMsqjZ/abDyqN5sjTjL302sh9MD+jwgeBB
                        ITg0UAKiaOJsIfjQUe/794ZwQ5sYInzRRlbXg/BwzLA1749Zy6zpmmt8XpHgdVjdfJIU/tyIk9R+365d5wrDR8sKvL0nHNF1fDzvV2sdwgn
                        H3L1FirMnxAxbbRp9Me1x3DxtpfbzXL7e1RyXMY5pVs/1es02d444VA/0Fbw4jqyihwvN6hGbGFqZTGgpw+19PLR0NGYnms3JjcL4tsORn
                        xoh3tuDGFrZQTCWldQpGly+s1Bgndi090/aF2YXJSo5HJiLd8NVS8zXdlxoLigDCbx9dEjn1H8hDB2bN5P1vw16D97lZJCxlGVQE9wsh
                        F8NIXirdE5FlyHIlcGaKvd11Lsm/McyYcE0muyNTQiesj12DHIUSefuyrIlpk70BVvNa4bfY8LH2j5mMLwx7MBzYy5/50hhfDcxWEh4a8nQ
                        1FPrK00mCzQhxwxbUhvzQNnPpWQgg/BLHcE1hSQwbMn2heMxvl746tyYnt5kPk7kdbj5zTxQDj1QZznAQdOXAguMRjFxzM6qC4JeoAm2uT5mj
                        NQgOFQknZcFGXZQKySd1w0B4pDOQyUgGPC6Lum8LgiQPtftgCeAfQYkvgTY94DEN0H2p0Dij0H2OiDxQoi9Eky8FGZ/F0j8ZYj9bTDx12H27wHJ
                        CDx/rY4HEfEEoM8AWY0l4OHh4eHh4RGsVfwPP+3PPBW7WDgAAAAASUVORK5CYII=" alt="logo" className="mb-1" />
                    <button className="navbar-toggler" data-bs-toggle="offcanvas" data-bs-target="#offcanvasDarkNavbar">
                        <span className="navbar-icon"></span>
                    </button>
                    <div className="offcanvas offcanvas-start bg-123458" tabIndex="-1" id="offcanvasDarkNavbar">
                        <div className="offcanvas-header">
                            <div className="offcanvas-title" id="offcanvasDarkNavbarLabel">
                                <img width={50} height={50}
                                    src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsTAAALEwEAmp
                    wYAAAEWElEQVR4nO1ZW4hcRRC98RUVX/GBikQRX2DABygiakQREVT8EBEF8yHir58Bf/TDD/FH93PFBwiKDCq44BKZqeqdbDJTd
                    XPd3UzX3YRBo6ASxUeCz2jiXunZ3M2dO7c7M7PLzOxuH+ifoXq66nRV9em+QeDh4TEMKKVOE4IxzTguUjojWE+Ymdl5iTAoYUxag
                    3D3fFS9PFgPiMOpWzXjN0vBHx+a8fu4jncEaxnC8LQw/JUPfokEwiMNgmeDtYZSqXSqJnjVFnhBNoxHUXR6sBYwz+WLNEG52+AzJ
                    Oxs1CuXFv1nN/ODUUCjXrlJGA4UOwlHdR1fEMLnheBfi823DVa3rUoCdIhPCuEfllr/WQjuT22lru4Wxh+LMwH+1gTbVg0BSZJs0
                    Awva4KF4uBBz1H16vw8HVY3a8YvrEERjJleMtIENGnyPM34qaOuP4ui8vm2+aLUOUL4iYOEHY3p6U0jSYBQ5UbN0LTs+oIQvJIkL52
                    S2jebkxs14ZvC+JrJmvR3Y2NsrRlkWWOoBDQIHxbCw44afiZrP1urXKEZ6hm79/LHXszwhBD+2evpMVACkiTZIIzbNeF/Fke+02H59
                    rbAqHKPJjjYmd44UavVzsraCqtbilTjSBAgi/X6sb1esSasLmub4zzyWqdDNd8j9kfqYiGYGikCpKau1YTiaHYfZHfza6XO1ATvduO4+
                    V9TItn1Wv2C8Z2RIEBYPSQMv1oWPmZKos0+Kl8pBHt620E4ENenrutY22QQw9GhEaCNclsMsqjZ/abDyqN5sjTjL302sh9MD+jwgeBB
                    ITg0UAKiaOJsIfjQUe/794ZwQ5sYInzRRlbXg/BwzLA1749Zy6zpmmt8XpHgdVjdfJIU/tyIk9R+365d5wrDR8sKvL0nHNF1fDzvV2sdwgn
                    H3L1FirMnxAxbbRp9Me1x3DxtpfbzXL7e1RyXMY5pVs/1es02d444VA/0Fbw4jqyihwvN6hGbGFqZTGgpw+19PLR0NGYnms3JjcL4tsORn
                    xoh3tuDGFrZQTCWldQpGly+s1Bgndi090/aF2YXJSo5HJiLd8NVS8zXdlxoLigDCbx9dEjn1H8hDB2bN5P1vw16D97lZJCxlGVQE9wsh
                    F8NIXirdE5FlyHIlcGaKvd11Lsm/McyYcE0muyNTQiesj12DHIUSefuyrIlpk70BVvNa4bfY8LH2j5mMLwx7MBzYy5/50hhfDcxWEh4a8nQ
                    1FPrK00mCzQhxwxbUhvzQNnPpWQgg/BLHcE1hSQwbMn2heMxvl746tyYnt5kPk7kdbj5zTxQDj1QZznAQdOXAguMRjFxzM6qC4JeoAm2uT5mj
                    NQgOFQknZcFGXZQKySd1w0B4pDOQyUgGPC6Lum8LgiQPtftgCeAfQYkvgTY94DEN0H2p0Dij0H2OiDxQoi9Eky8FGZ/F0j8ZYj9bTDx12H27wHJ
                    CDx/rY4HEfEEoM8AWY0l4OHh4eHh4RGsVfwPP+3PPBW7WDgAAAAASUVORK5CYII=" alt="logo" className="mb-1" />
                            </div>
                            <button type="button" className="btn-close btn-close-white" data-bs-dismiss="offcanvas"></button>
                        </div>
                        <div className="offcanvas-body justify-content-end">
                            <ul className="navbar-nav">
                                <li className="nav-item pe-md-3 mb-1">
                                    <Link className={`nav-link nav-link-hover text-D4C9BE ${location.pathname === "/" ? "active-D4C9BE" : ""} `} to={"/"}>Home</Link>
                                </li>
                                <li className="nav-item  pe-md-3 mb-1 dropdown">
                                    <p className="nav-link nav-link-hover text-D4C9BE dropdown-toggle mb-0" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                        Login
                                    </p>
                                    <ul className="dropdown-menu bg-123458 p-2">
                                        <li className="mb-1">
                                            <Link className={`nav-link nav-link-hover text-D4C9BE ${location.pathname === "/user/login" ? "active-D4C9BE" : ""}`} to={"/user/login"}>User</Link>
                                        </li>
                                        <li className="mb-1">
                                            <Link className={`nav-link nav-link-hover text-D4C9BE ${location.pathname === "/admin/login" ? "active-D4C9BE" : ""}`} to={"/admin/login"}>Admin</Link>
                                        </li>
                                    </ul>
                                </li>
                                <li className="nav-item pe-md-3 mb-1">
                                    <Link className={`nav-link nav-link-hover text-D4C9BE ${location.pathname === "/user/register" ? "active-D4C9BE" : ""}`} to={"/user/register"}>Register</Link>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </nav >
        </>
    )
}